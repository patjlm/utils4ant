# utils4ant

Welcome to Utils4Ant!

This project (will) host the following utils (tasks, types, mapper, filters, ...) for Ant

Java API
* AntLauncher : how to launch ant from Java

Tasks
* FastCopy : file copy, faster than ant's original
* Timer : measure elapsed time for nested tasks
* GC : allows to run a garbage collection in the Ant JVM

Filters
* EchoFilter : outputs data during ant filtering. Useful for debuging
* PatternFilter : filters based on a file pattern matcher
* ConditionFilter : allows test (if / elseif / else) sections in a resource content going through a filterchain

Mappers
* EchoMapper : outputs data during ant mapping. Useful for debuging

Selectors
* EchoSelector : outputs data during ant resource selection. Useful for debuging
