# syd-rpc

## Demo

 0. *(from /syd-rpc)*
 1. compile fr.mxyns.rpc.compiler
 2. run 'java fr.mxyns.rpc.compiler.Compiler -o build -i src/fr/mxyns/rpc/example/ -d Client.java IVoiture.java Main.java Trajet.java Voiture_dist.java -I Voiture.irpc -c -r fr.mxyns.rpc.example.Main -ra __server'
 3. generated classes should be located and compiled in /syd-rpc/build and example program should run in 'server' mode

## Features
   - compiles .irpc to .java classes
   - generated classes use Sockets to query method invokes on the server
   - supports attributes declaration / manipulation
   - has instance state sharing between Server and Client
   - should support any method / argument combination

## Launch arguments
   - `-i` input root / project folder containing classes and .irpc
   - `-o` output path / directory in which the compiled program will be generated
   - `-I` list of paths to .irpc files to compile, separated by spaces
   - `-d` list of paths/files to deploy (deploy required project files that need to be copied to output path)
   - `-c` if flag is set, project in 'output path' is compiled using javac (not required when using `-r`)
   - `-r` package.class to execute after compilation
   - `-ra` arguments to pass to executed program (replace `-` by `__`. e.g. : `-r com.example.Main -ra __server => java com.example.Main -server`)

## .irpc format
.irpc are summaries of the interfaces implemented by "distributed" classes
### .irpc file content : (check [the provided example](/src/fr/mxyns/rpc/example/Voiture.irpc))
`package`
`interfaceName` (must be the name of the "distributed" class)
`list of attributes`
`list of methods`

### Attributes
identical to Java attribute declaration, preceded by a `@` and **without`;` at the end**
       -  `@modifiers type name = default_value`
       -  `@modifiers type name`
       *e.g.* : `public String brand = "Renaut"`| (initialized)
         or : `public String brand`  | (not initialized)
### Methods
identical to Java method declaration in interfaces, **without `;` at the end**
       - `modifiers returnType methodName(ArgType1 arg1, ArgType2 arg2...)`
       *e.g.* : `String reversedName(String prefix, String suffix)`  | (most of the time you won't have any modifiers, just like in Java interfaces)

*NB* : `TARGET` and `PORT` used by the server and the generated classes are set in [Server.java](/src/fr/mxyns/rpc/compiler/Server.java) as globals

##
Known problems :
   + methods with 0 arguments require us to write a `null` value as a workaround or else the programs hangs on the receiving `.readUTF()`
   + Dummy class needed if we don't want errors showing up in the IDE when using classes that will be generated from .irpc
   + A socket is opened and closed for each method call which is stupid and slow

Not tested : 
   + everything that isn't in the example, including multiple directories / classes
