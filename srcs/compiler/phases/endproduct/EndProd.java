package compiler.phases.endproduct;

import compiler.data.asmcode.AsmInstr;
import compiler.data.asmcode.Code;
import compiler.data.chunk.DataChunk;
import compiler.phases.Phase;
import compiler.phases.asmcode.AsmGen;
import compiler.phases.chunks.Chunks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Vector;

public class EndProd extends Phase {
    /**
     * Constructs a new phase of a compiler. If logging of this phase has been
     * requested, it prepares a logger using the phase name for naming the XML and
     * XSL files as well as for the topmost XML element within the XML file.
     */
    public EndProd() {
        super("endproduct");
        functions = new Vector<>();
    }

    Vector<EndFunction> functions;

    public void finishCode(){
        functions.addAll(EndFunction.getSystemFunctions());
        for (Code c : AsmGen.codes){
            functions.add(new EndFunction(c));
        }
    }

    public void log(){
        String destinationFileName = compiler.Main.cmdLineArgValue("--dst-file-name");
        try{
            FileWriter fwStream = new FileWriter(destinationFileName);
            BufferedWriter writer = new BufferedWriter(fwStream);
            writeCode(writer);
            writer.close();
            fwStream.close();
        }catch(Exception e){System.out.println(e);}
    }

    public void writeCode(BufferedWriter writer){
        writeDataChunks(writer);
        writeCodeChunks(writer);
    }

    private void writeDataChunks(BufferedWriter writer){
        try {
            writer.write("\t\tLOC\tData_Segment");
            writer.newLine();
            writer.write("\t\tGREG\t@");
            writer.newLine();
            //TODO: Maybe fix
            for (DataChunk chunk : Chunks.dataChunks){
                if (chunk.init != null){
                    writer.write(chunk.label.name);
                    writer.write('\t');
                    writer.write("BYTE\t");
                    writer.write(chunk.init);
                    writer.newLine();
                }
            }
            writer.newLine();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeCodeChunks(BufferedWriter writer){
        // TODO: Something
        try {
            writer.write("\t\tLOC\t#100");
            writer.newLine();
            writer.newLine();
            //TODO: fix
            for (EndFunction fn : functions){
                for (String str : fn.instructions){
                    writer.write(str);
                    writer.newLine();
                }
            }
            writer.newLine();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
