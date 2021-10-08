java -cp build fr.mxyns.rpc.compiler.Compiler -o ./rpc_out_tp -i ./example_tp/ -d Client.java IMatlab.java Matlab_dist.java Result.java -I Matlab.irpc -c 
echo "=== Run client using : java -cp rpc_out_tp rpc.Client"
echo "=== Running server..."
java -cp rpc_out_tp fr.mxyns.rpc.compiler.Server