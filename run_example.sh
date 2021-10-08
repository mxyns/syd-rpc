java -cp build fr.mxyns.rpc.compiler.Compiler -o ./rpc_out -i ./example/ -d Client.java IVoiture.java Main.java Trajet.java Voiture_dist.java -I Voiture.irpc -c
echo "=== Run client using : java -cp rpc_out fr.mxyns.rpc.example.Main -client"
echo "=== Running server..."
java -cp rpc_out fr.mxyns.rpc.example.Main -server