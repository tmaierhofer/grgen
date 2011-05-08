" Vim syntax file
" Language:     GrGen Rule Set
" Maintainer:   Sebastian Buchwald <Sebastian.Buchwald@kit.edu>
" Last Change:  2011 May 7

if version < 600
  syntax clear
elseif !exists("b:current_syntax")
  finish
endif

syn spell notoplevel

syn keyword grgKeyWords            alternative delete dpo emit eval exact false hom
syn keyword grgKeyWords            if induced iterated modify multiple negative prio
syn keyword grgKeyWords            replace return true typeof var
syn keyword grgKeyWords            pattern rule test nextgroup=grgRulePrefix
syn keyword grgKeyWords            exec using nextgroup=grgIgnoreStatement
syn match   grgVariable            "\h\w*"
syn match   grgPreProc             "^#include"
syn match   grgTypePrefix          ":\(\s\|\n\)*" nextgroup=grgTypeDecl,grgReturnTypes,grgPatternInstance,grgKeyWords
syn match   grgTypeDecl            "\h\w*\(\(\s\|\n\)*\\\(\(\s\|\n\)*\h\w*\|(\(,\=\(\s\|\n\)*\h\w*\)*)\)\)\=" contained contains=grgType
syn match   grgType                "\h\w*" contained
syn match   grgPatternInstance     "\h\w*\(\s\|\n\)*(" contains=grgRule contained
syn match   grgReturnTypes         "(\(,\=\(\s\|\n\)*\h\w*\)*)" contains=grgType contained
syn region  grgComment             start="/\*" end="\*/" contains=@Spell
syn region  grgComment             start="//" end="$" contains=@Spell
syn match   grgString              "\"\([^\\"]\|\\\\\|\\\"\|\\n\|\\t\)*\"" contains=grgSpecialChar,@Spell
syn match   grgSpecialChar         "\\\"\|\\\\\|\\n\|\\t"
syn match   grgRulePrefix          "\(\s\|\n\)*" nextgroup=grgRule contained
syn match   grgRule                "\h\w*" nextgroup=grgRulePostfix contained
syn match   grgRulePostfix         "(\(\n\|[^{]\)*" contains=grgVariable,grgTypePrefix,grgOriginalType,grgKeyWords,grgComment contained
syn match   grgOriginalType        "<\(\s\|\n\)*\h\w*\(\s\|\n\)*>" contains=grgType contained
syn match   grgAlternativePattern  "\h\w*\(\s\|\n\)*{" contains=grgAlternative
syn match   grgAlternative         "\h\w*" contained
syn match   grgAttributePattern    "\.\h\w*"
syn match   grgVariable            "\h\w*" contained
syn match   grgEnumPattern         "\h\w*\(\s\|\n\)*::" contains=grgEnum
syn match   grgEnum                "\h\w*" contained
syn match   grgIgnoreStatement     "\(\n\|[^;]\)*;" contains=grgDelimiter contained
syn match   grgDelimiter           ";"

hi def link grgKeyWords    Statement
hi def link grgComment     Comment
hi def link grgPreProc     PreProc
hi def link grgString      String
hi def link grgSpecialChar SpecialChar
hi def link grgVariable    Identifier
hi def link grgType        Type
hi def link grgEnum        Type
hi def link grgRule        Function
hi def link grgAlternative Function
hi def link grgDelimiter   Delimiter

let b:current_syntax = "grg"
