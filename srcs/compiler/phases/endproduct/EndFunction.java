package compiler.phases.endproduct;

import compiler.data.asmcode.AsmInstr;
import compiler.data.asmcode.AsmLABEL;
import compiler.data.asmcode.Code;

import java.util.Vector;

public class EndFunction {

    Code code;
    public Vector<String> instructions;

    EndFunction(Code code){
        this.code = code;
        instructions = new Vector<>();
        transformFunction();
    }

    private EndFunction(){
        instructions = new Vector<>();
    }

    private void transformFunction(){
        addPrologue();
        for (AsmInstr instr : code.instrs){
            if (instr instanceof AsmLABEL){
                instructions.add(((AsmLABEL) instr).label.name + "\tADD $0,$0,0");
            }
            else {
                instructions.add("\t\t" + instr.toString(code.regs));
            }
        }
        addEpilogue();
        replaceRegisters();
    }

    private void addPrologue(){
        long offset = code.tempSize + code.frame.size;
        long low = offset & ((1<<16) - 1);
        offset = offset >> 16;
        long medLow = offset & ((1<<16) - 1);
        offset = offset >> 16;
        long medhigh = offset & ((1<<16) - 1);
        offset = offset >> 16;
        long high = offset & ((1<<16) - 1);
        instructions.add(code.frame.label.name + "\tSUB $0,$254,$253");
        instructions.add("\t\tSUB $0,$253,$0");
        instructions.add("\t\tSTO $253,$0,0");
        instructions.add("\t\tSUB $0,$0,8");
        instructions.add("\t\tGET $1,rJ");
        instructions.add("\t\tSTO $1,$0,0");
        instructions.add("\t\tOR $253,$254,0");
        instructions.add("\t\tSETL $0," + low);
        if (medLow > 0) {
            instructions.add("\t\tINCML $0," + medLow);
        }
        if (medhigh > 0) {
            instructions.add("\t\tINCMH $0," + medhigh);
        }
        if (high > 0) {
            instructions.add("\t\tINCH $0," + high);
        }
        instructions.add("\t\tSUB $254,$254,$0");
        instructions.add("\t\tJMP " + code.entryLabel.name);
    }

    private void addEpilogue(){
        //long offset = code.tempSize + code.frame.size;
        instructions.add(code.exitLabel.name + "\tOR $254,$253,0");
        instructions.add("\t\tSTO $"+code.regs.get(code.frame.RV)+",$254");
        instructions.add("\t\tLDO $0,$254,0");
        instructions.add("\t\tOR $253,$0,0");
        instructions.add("\t\tPOP 16");
    }

    private void replaceRegisters(){
        Vector<String> newInst = new Vector<>();
        for (String str : instructions){
            String s = str.replaceAll("\\$253","fP");
            newInst.add(s.replaceAll("\\$254","sP"));
        }
        instructions = newInst;
    }

    public static Vector<EndFunction> getSystemFunctions(){
        Vector<EndFunction> fns = new Vector<>();
        fns.add(addBootstrapStart());
        fns.add(addNewFun());
        fns.add(addDelFun());
        fns.add(putStrFun());
        fns.add(putIntFun());
        fns.add(putCharFun());
        return fns;
    }

    public static EndFunction addBootstrapStart(){
        EndFunction fn = new EndFunction();
        Vector<String> instrs = fn.instructions;
        instrs.add("Main\tSETH fP,1");
        instrs.add("\t\tSETH sP,1");
        instrs.add("\t\tPUSHJ $16,_main");
        instrs.add("\t\tTRAP\t0,Halt,0");
        return fn;
    }

    public static EndFunction addNewFun(){
        EndFunction fn = new EndFunction();
        Vector<String> instrs = fn.instructions;
        return fn;
    }

    public static EndFunction addDelFun(){
        EndFunction fn = new EndFunction();
        Vector<String> instrs = fn.instructions;
        instrs.add("_del\tPOP 16");
        return fn;
    }

    public static EndFunction putStrFun(){
        EndFunction fn = new EndFunction();
        Vector<String> instrs = fn.instructions;
        instrs.add("_putString\tLDO $255,sP,8");
        instrs.add("\t\tTRAP 0,Fputs,StdOut");
        instrs.add("\t\tPOP 16");
        return fn;
    }

    public static EndFunction putIntFun(){
        EndFunction fn = new EndFunction();
        Vector<String> instrs = fn.instructions;
        instrs.add("_putInt\tLDO $255,sP,8");
        instrs.add("\t\tPOP 16");
        return fn;
    }

    public static EndFunction putCharFun(){
        EndFunction fn = new EndFunction();
        Vector<String> instrs = fn.instructions;
        instrs.add("_putChar\tLDO $255,sP,8");
        instrs.add("\t\tPOP 16");
        return fn;
    }
}
