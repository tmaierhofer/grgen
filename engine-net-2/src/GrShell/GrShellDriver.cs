/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 4.5
 * Copyright (C) 2003-2019 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

// by Moritz Kroll, Edgar Jakumeit

using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;

namespace de.unika.ipd.grGen.grShell
{
    public class GrShellDriver
    {
        public LinkedList<GrShellTokenManager> TokenSourceStack = new LinkedList<GrShellTokenManager>();
        public Stack<bool> ifNesting = new Stack<bool>();

        public bool Quitting = false;
        public bool Eof = false;
        public bool readFromConsole = false;

        public bool showIncludes = false;

        GrShell grShell;
        GrShellImpl impl;

        GrShellDriver(GrShell grShell, GrShellImpl impl)
        {
            this.grShell = grShell;
            this.impl = impl;
        }

        static int Main(string[] args)
        {
            String command = null;
            ArrayList scriptFilename = new ArrayList();
            bool showUsage = false;
            bool nonDebugNonGuiExitOnError = false;
            bool showIncludes = false;
            int errorCode = 0; // 0==success, the return value

            GrShellImpl.PrintVersion();

            for(int i = 0; i < args.Length; i++)
            {
                if(args[i][0] == '-')
                {
                    if(args[i] == "-C")
                    {
                        if(command != null)
                        {
                            Console.WriteLine("Another command has already been specified with -C!");
                            errorCode = -1;
                            showUsage = true;
                            break;
                        }
                        if(i + 1 >= args.Length)
                        {
                            Console.WriteLine("Missing parameter for -C option!");
                            errorCode = -1;
                            showUsage = true;
                            break;
                        }
                        command = args[i + 1];
                        Console.WriteLine("Will execute: \"" + command + "\"");
                        i++;
                    }
                    else if(args[i] == "-N")
                    {
                        nonDebugNonGuiExitOnError = true;
                    }
                    else if(args[i] == "-SI")
                    {
                        showIncludes = true;
                    }
                    else if(args[i] == "--help")
                    {
                        Console.WriteLine("Displays help");
                        showUsage = true;
                        break;
                    }
                    else
                    {
                        Console.WriteLine("Illegal option: " + args[i]);
                        showUsage = true;
                        errorCode = -1;
                        break;
                    }
                }
                else
                {
                    String filename = args[i];
                    if(!File.Exists(filename))
                    {
                        filename = filename + ".grs";
                        if(!File.Exists(filename))
                        {
                            Console.WriteLine("The script file \"" + args[i] + "\" or \"" + filename + "\" does not exist!");
                            showUsage = true;
                            errorCode = -1;
                            break;
                        }
                    }
                    scriptFilename.Add(filename);
                }
            }

            if(showUsage)
            {
                Console.WriteLine("Usage: GrShell [-C <command>] [<grs-file>]...");
                Console.WriteLine("If called without options, GrShell is started awaiting user input. (Type help for help.)");
                Console.WriteLine("Options:");
                Console.WriteLine("  -C <command> Specifies a command to be executed >first<. Using");
                Console.WriteLine("               ';;' as a delimiter it can actually contain multiple shell commands");
                Console.WriteLine("               Use '#\u00A7' in that case to terminate contained exec.");
                Console.WriteLine("  -N           non-interactive non-gui shell which exits on error instead of waiting for user input");
                Console.WriteLine("  -SI          prints to console when includes are entered and exited");
                Console.WriteLine("  <grs-file>   Includes the grs-file(s) in the given order");
                return errorCode;
            }

            IWorkaround workaround = WorkaroundManager.Workaround;
            TextReader reader;
            bool showPrompt;
            bool readFromConsole;

            if(command != null)
            {
                reader = new StringReader(command);
                showPrompt = false;
                readFromConsole = false;
            }
            else if(scriptFilename.Count != 0)
            {
                try
                {
                    reader = new StreamReader((String)scriptFilename[0]);
                }
                catch(Exception e)
                {
                    Console.WriteLine("Unable to read file \"" + scriptFilename[0] + "\": " + e.Message);
                    return -1;
                }
                scriptFilename.RemoveAt(0);
                showPrompt = false;
                readFromConsole = false;
            }
            else
            {
                reader = workaround.In;
                showPrompt = true;
                readFromConsole = true;
            }

            GrShell shell = new GrShell(reader);
            GrShellImpl impl = new GrShellImpl();
            GrShellDriver driver = new GrShellDriver(shell, impl);
            shell.SetImpl(impl);
            shell.SetDriver(driver);
            driver.TokenSourceStack.AddFirst(shell.token_source);
            impl.nonDebugNonGuiExitOnError = nonDebugNonGuiExitOnError;
            driver.showIncludes = showIncludes;
            driver.readFromConsole = readFromConsole;
            try
            {
                driver.ifNesting.Push(true);
                while(!driver.Quitting && !driver.Eof)
                {
                    driver.ShowPromptAsNeeded(showPrompt);
                    bool noError = shell.ParseShellCommand();
                    if(!driver.readFromConsole && (driver.Eof || !noError))
                    {
                        if(nonDebugNonGuiExitOnError && !noError)
                        {
                            return -1;
                        }

                        if(scriptFilename.Count != 0)
                        {
                            TextReader newReader;
                            try
                            {
                                newReader = new StreamReader((String)scriptFilename[0]);
                            }
                            catch(Exception e)
                            {
                                Console.WriteLine("Unable to read file \"" + scriptFilename[0] + "\": " + e.Message);
                                return -1;
                            }
                            scriptFilename.RemoveAt(0);
                            shell.ReInit(newReader);
                            driver.Eof = false;
                            reader.Close();
                            reader = newReader;
                        }
                        else
                        {
                            shell.ReInit(workaround.In);
                            driver.TokenSourceStack.RemoveFirst();
                            driver.TokenSourceStack.AddFirst(shell.token_source);
                            showPrompt = true;
                            driver.readFromConsole = true;
                            driver.Eof = false;
                            reader.Close();
                        }
                    }
                }
                driver.ifNesting.Pop();
            }
            catch(Exception e)
            {
                Console.WriteLine("exit due to " + e.Message);
                errorCode = -2;
            }
            finally
            {
                impl.Cleanup();
            }
            return errorCode;
        }

        public void ShowPromptAsNeeded(bool ShowPrompt)
        {
            if(ShowPrompt) Console.Write("> ");
        }

        public bool Include(String filename, String from, String to)
        {
            try
            {
                if(showIncludes)
                    impl.debugOut.WriteLine("Including " + filename);
                TextReader reader = null;
                if (filename.EndsWith(".gz", StringComparison.InvariantCultureIgnoreCase)) {
                    FileStream filereader = new FileStream(filename, FileMode.Open,  FileAccess.Read);
                    reader = new StreamReader(new GZipStream(filereader, CompressionMode.Decompress));
                } else {
                    reader = new StreamReader(filename);
                }
                if(from != null || to != null)
                    reader = new FromToReader(reader, from, to);
                using(reader)
                {
                    SimpleCharStream charStream = new SimpleCharStream(reader);
                    GrShellTokenManager tokenSource = new GrShellTokenManager(charStream);
                    TokenSourceStack.AddFirst(tokenSource);
                    try
                    {
                        grShell.ReInit(tokenSource);
                        while(!Quitting && !Eof)
                        {
                            if(!grShell.ParseShellCommand())
                            {
                                impl.errOut.WriteLine("Shell command parsing failed in include of \"" + filename + "\" (at nesting level " + TokenSourceStack.Count + ")");
                                return false;
                            }
                        }
                        Eof = false;
                    }
                    finally
                    {
                        if(showIncludes)
                            impl.debugOut.WriteLine("Leaving " + filename);
                        TokenSourceStack.RemoveFirst();
                        grShell.ReInit(TokenSourceStack.First.Value);
                    }
                }
            }
            catch(Exception e)
            {
                impl.errOut.WriteLine("Error during include of \"" + filename + "\": " + e.Message);
                return false;
            }
            return true;
        }

        public void Quit()
        {
            impl.QuitDebugMode();

            Quitting = true;

            impl.debugOut.WriteLine("Bye!\n");
        }
    }
}
