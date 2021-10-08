package fr.mxyns.rpc.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fr.mxyns.rpc.compiler.RPCUtils.*;

public class Compiler {

    public static void main(String[] args) {

        try {

            // Parse arguments
            HashMap<String, String> argsMap = parseArgs(args);
            Path inputRoot = Path.of(getArg(argsMap, "-i", "."));
            Path outputPath = Path.of(getReqArg(argsMap, "-o"));
            String[] toCompileArr = getReqArgs(argsMap, "-I");
            Set<String> toDeploy = new HashSet<>(Arrays.asList(getArgs(argsMap, "-d", new String[] { inputRoot.toString() })));
            String toRun = getArg(argsMap, "-r", null);
            String toRunArgs = getArg(argsMap, "-ra", "").replaceAll("__", "-");
            boolean mustCompile = toRun != null || hasFlag(argsMap, "-c");

            // Print Arguments Summary
            System.out.println("Absolute Input Root : " + inputRoot.toAbsolutePath());
            System.out.println("Absolute Output Dir : " + outputPath.toAbsolutePath());
            System.out.println("To Compile Source : " + Arrays.toString(toCompileArr));
            System.out.println("To Deploy Source : " + toDeploy);
            System.out.println("Will try to compile project automatically : " + (mustCompile ? "Yes!" : "No."));
            System.out.println("Target to run : " + (toRun == null ? "None" : outputPath.resolve(toRun).toAbsolutePath()));
            System.out.println("With Args : " + toRunArgs);

            // Ignore every class that has the same name as the Interface RPC Descriptor (.irpc)
            String[] toIgnore = new String[toCompileArr.length];
            System.arraycopy(toCompileArr, 0, toIgnore, 0, toCompileArr.length);

            // Compile every interface descriptor provided
            for (String toCompileFile : toCompileArr) {
                Path toCompileFilePath = inputRoot.resolve(toCompileFile);
                String stub = generateStub(toCompileFilePath);

                String fileOutPathNoExt;
                if (toCompileFilePath.getFileName().toString().contains("."))
                    fileOutPathNoExt = toCompileFile.substring(0, toCompileFile.lastIndexOf("."));
                else
                    fileOutPathNoExt = toCompileFile;

                Path stubPath = outputPath.resolve(fileOutPathNoExt + ".java");

                Files.createDirectories(stubPath.getParent());
                Files.write(stubPath, stub.getBytes());
            }

            deployBoot(inputRoot, toDeploy, toIgnore, outputPath);

            List<String> finishCommands = new ArrayList<>();
            if (mustCompile) finishCommands.add("javac -d . *.java");
            if (toRun != null) finishCommands.add("java " + toRun + " " + toRunArgs.trim());

            for (String cmd : finishCommands) {
                Process process = Runtime.getRuntime().exec(cmd, null, outputPath.toAbsolutePath().toFile());

                BufferedReader lineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                lineReader.lines().forEach(System.out::println);

                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                errorReader.lines().forEach(System.out::println);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deployBoot(Path inputRoot, Set<String> toDeploy, String[] toIgnore, Path outputPath) throws IOException {

        Set<String> toIgnoreSet = new HashSet<>(Arrays.asList(toIgnore));
        // Deploy mandatory Server class
        Path srcPath = null;

        String[] libDeploy = new String[] {
            "Server.java",
            "RPCUtils.java"
        };

        Files.createDirectories(outputPath);

        // Deploy files that are mandatory for the compiled program to work
        for (String lib : libDeploy) {
            srcPath = Path.of("./src", Compiler.class.getPackageName().replaceAll("\\.", "/"), lib);
            try {
                Files.copy(srcPath, outputPath.resolve(lib), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                System.err.println("[err] Couldn't copy " + srcPath.toAbsolutePath() + " to " + outputPath.resolve(lib).toAbsolutePath() + ".\n" +
                                   "Please locate the file and do it manually as it is required for the compiled program to work properly.");
            }
        }

        // If directories given, list files in them and add to deployed files
        for (String path : toDeploy) {

            File file = Path.of(path).toFile();
            File[] fileList = file.listFiles();

            if (file.isDirectory() && fileList != null && fileList.length > 0)
                for (File fileInPath : fileList)
                    toDeploy.add(fileInPath.toPath().toString());
        }

        HashSet<String> interfaceToIgnore = new HashSet<>();
        for (String ignored : toIgnoreSet)
            interfaceToIgnore.add(Files.readAllLines(inputRoot.resolve(ignored)).get(1) + ".java");

        // Deploy files (copy them to outputPath) if not ignored
        for (String file : toDeploy) {

            srcPath = inputRoot.resolve(file);
            if (interfaceToIgnore.contains(srcPath.getFileName().toString())) continue;

            Files.createDirectories(outputPath);
            try {
                Files.copy(srcPath, outputPath.resolve(file), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                System.err.println("[err] Couldn't copy " + srcPath.toAbsolutePath() + " to " + outputPath.resolve(file).toAbsolutePath() + ".\n" +
                                   "Please locate the file and do it manually.");
            }
        }
    }

    private static String generateStub(Path toCompileFilePath) throws IOException {

        List<String> lines = Files.readAllLines(toCompileFilePath);
        List<Method> methods = new ArrayList<>();
        List<Attribute> attributes = new ArrayList<>();

        String packageName = lines.get(0);
        String interfaceName = lines.get(1);
        for (int i = 2; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (line.equals(""))
                continue;
            else if (line.startsWith("@"))
                attributes.add(parseAttribute(line));
            else
                methods.add(parseMethod(line));
        }

        String stub = """
            package %s;
                            
            import java.io.IOException;
            import java.io.Serializable;
            import fr.mxyns.rpc.compiler.RPCUtils;
            import %s.I%s;
                        
            public class %s implements I%s {
                  
                %s
                           
            """;

        String attributesCode = "", attributesToStringCode = "";
        for (Attribute a : attributes)
            if (a != null) {
                attributesCode += a.genRPC() + "\n";
                attributesToStringCode += "\"(" + a.modifiers + ") " + a.name + " = \" + this." + a.name + " + \",\\n\"";
            }

        stub = String.format(stub,
                             packageName.trim(),
                             packageName.trim(),
                             interfaceName.trim(),
                             interfaceName.trim(),
                             interfaceName.trim(),
                             attributesCode.trim()
                            );

        for (Method m : methods)
            if (m != null)
                stub += m.genRPC();

        stub += String.format("""
                                      
                                      @Override
                                      public String toString() {
                                          return "%s = {\\n" +
                                              %s
                                          + "}";
                                      }
                                              
                                  """,
                              interfaceName,
                              attributesToStringCode.length() > 0 ? attributesToStringCode : "\"\""
                             );

        stub += "}\n";

        return stub;
    }

    private static Attribute parseAttribute(String line) {

        line = line.replaceAll("[;@]", "").trim();

        String defaultValue = null;
        if (line.contains("=")) {
            String[] shards = line.split("=");
            defaultValue = Arrays.stream(shards).skip(1).collect(Collectors.joining("="));
            line = shards[0];
        }

        String[] shards = line.split(" ");
        String modifiers = "";
        if (shards.length > 2)
            modifiers = IntStream.range(0, shards.length - 2).mapToObj(i -> shards[i]).collect(Collectors.joining(" "));

        return new Attribute(modifiers, shards[shards.length - 2], shards[shards.length - 1], defaultValue);
    }

    private static Method parseMethod(String line) {


        String[] shards = line.split(" ");
        String returnType = shards[0];
        String rest = Arrays.stream(shards).skip(1).collect(Collectors.joining(" "));

        shards = rest.split("\\(");
        String methodName = shards[0];

        rest = shards[1].replaceAll("\\)", "");
        shards = rest.split(",");

        String[][] methodArgs = new String[shards.length][2];
        boolean empty = true;
        for (int j = 0; j < shards.length; j++) {
            String[] subShards = shards[j].trim().split(" ");
            if (subShards.length == 2) {
                empty = false;
                methodArgs[j][0] = subShards[0];
                methodArgs[j][1] = subShards[1];
            }
        }

        return new Method(returnType.trim(), methodName.trim(), empty ? null : methodArgs);
    }

    private static class Method {

        String returnType;
        String[][] args;
        String name;

        Method(String returnType, String name, String[][] args) {

            this.returnType = returnType;
            this.name = name;
            this.args = args;
        }

        public String genRPC() {

            String template = """
                    @Override
                    public %s %s(%s) {
                        try {
                            %s RPCUtils.genericFunctionCall(Server.TARGET, Server.COMM_PORT, this, "%s"%s);
                        } catch (IOException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                                
                        return %s;
                    }

                """;

            String methodArguments = "";
            String methodCallArguments = "";

            if (args != null)
                for (int i = 0; i < args.length; i++) {
                    methodArguments += args[i][0] + " " + args[i][1];
                    methodCallArguments += args[i][1];
                    if (i != args.length - 1) {
                        methodArguments += ", ";
                        methodCallArguments += ", ";
                    }
                }

            boolean returnsObject = !returnType.equals("void");

            return String.format(template,
                                 returnType,
                                 name,
                                 methodArguments,
                                 returnsObject ? "return (" + returnType + ")" : "",
                                 name,
                                 args != null ? ", " + methodCallArguments : "",
                                 returnsObject ? "null" : "");
        }
    }

    private static class Attribute {

        String type;
        String name;
        String defaultValue;
        String modifiers;

        Attribute(String modifiers, String type, String name, String defaultValue) {

            this.modifiers = modifiers;
            this.type = type;
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public String genRPC() {

            String template = """
                %s %s %s %s %s
                """;

            return String.format(template,
                                 modifiers.trim(),
                                 type.trim(),
                                 name.trim(),
                                 defaultValue != null ? "=" : "",
                                 defaultValue != null ? defaultValue.trim() : ""
                                ).trim() + ";";
        }
    }
}
