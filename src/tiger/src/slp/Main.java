package slp;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.HashSet;

import javafx.scene.control.Tab;
import slp.Slp.Exp;
import slp.Slp.Exp.Eseq;
import slp.Slp.Exp.Id;
import slp.Slp.Exp.Num;
import slp.Slp.Exp.Op;
import slp.Slp.ExpList;
import slp.Slp.Stm;
import slp.Slp.Table;
import util.Bug;
import util.Todo;
import control.Control;

public class Main {
    // ///////////////////////////////////////////
    // maximum number of args

    // Lab1 Exercise3
    private int maxArgsExp(Exp.T exp) {
        // Added by Yang.Han
        // May be EseqExp
        if (exp instanceof Exp.Eseq) {
            int n1 = maxArgsStm(((Exp.Eseq) exp).stm);
            int n2 = maxArgsExp(((Exp.Eseq) exp).exp);
            return n1 >= n2 ? n1 : n2;
            // May be OpExp
        } else if (exp instanceof Exp.Op) {
            int n1 = maxArgsExp(((Exp.Op) exp).left);
            int n2 = maxArgsExp(((Op) exp).right);
            return n1 >= n2 ? n1 : n2;
            // Other Exp won't have PrintStm
        } else {
            return 0;
        }
    }

    // Lab1 Exercise3
    // added by Yang.Han
    // I think ExpList should be processed in a single method.
    private int maxArgsExpList(ExpList.T explist) {
        // May be a Pair
        if (explist instanceof ExpList.Pair) {
            int n1 = maxArgsExp(((ExpList.Pair) explist).exp);
            int n2 = maxArgsExpList(((ExpList.Pair) explist).list);
            return n1 >= n2 ? n1 : n2;
            // May be Last
        } else {
            return maxArgsExp(((ExpList.Last) explist).exp);
        }
    }

    // Lab1 Exercise3
    // Added by Yang.Han
    // Also will be used in Lab1 Exercise 4. (I change the way
    // to print, now it's only used in Exercise 3).
    private int countExpList(ExpList.T explist) {
        if (explist instanceof ExpList.Last) {
            return 1;
        } else {
            return 1 + countExpList(((ExpList.Pair) explist).list);
        }
    }

    // Lab1 Exercise3
    private int maxArgsStm(Stm.T stm) {
        if (stm instanceof Stm.Compound) {
            Stm.Compound s = (Stm.Compound) stm;
            int n1 = maxArgsStm(s.s1);
            int n2 = maxArgsStm(s.s2);
            return n1 >= n2 ? n1 : n2;
        } else if (stm instanceof Stm.Assign) {
            // Added by Yang.Han Begin
            return maxArgsExp(((Stm.Assign) stm).exp);
            // Add by Yang.Han End
        } else if (stm instanceof Stm.Print) {
            // Added by Yang.Han
            // If we get a PrintStm, we will do two process
            // 1. Count current PrintStm's args
            // 2. Traverse ExpList recursively because there may be PrintStm in it.
            // 3. One more thing we need to do is that get the max one between the current PrintStm's
            // args number and the max one we get by traverse ExpList recursively.
            int max = countExpList(((Stm.Print) stm).explist);
            ExpList.T expListTmp = ((Stm.Print) stm).explist;
            if (expListTmp instanceof ExpList.Pair) {
                int n1 = maxArgsExp(((ExpList.Pair) expListTmp).exp);
                int n2 = maxArgsExpList(((ExpList.Pair) expListTmp).list);
                int n = n1 >= n2 ? n1 : n2;
                return max >= n ? max : n;
            } else {
                int n = maxArgsExp(((ExpList.Last) expListTmp).exp);
                return max >= n ? max : n;
            }
            // Added by Yang.Han end.
        } else
            new Bug();
        return 0;
    }

    // ////////////////////////////////////////
    // interpreter

    // Lab1 Exercise4

    Table header = null;

    private void interpExp(Exp.T exp) {
        Table currentId = header;
        if (exp instanceof Exp.Num) {
            header = new Table(Integer.toString(((Num) exp).num), ((Num) exp).num, header);
        } else if (exp instanceof Exp.Id) {
            header = new Table(((Id) exp).id, Table.lookup(header, ((Id) exp).id).getValue(), header);
        } else if (exp instanceof Exp.Op) {
            interpExp(((Op) exp).left);
            int leftValue = header.getValue();
            interpExp(((Op) exp).right);
            int rightValue = header.getValue();
            switch (((Op) exp).op) {
                case ADD:
                    header = new Table(currentId.getId(), leftValue + rightValue, header);
                    break;
                case SUB:
                    header = new Table(currentId.getId(), leftValue - rightValue, header);
                    break;
                case TIMES:
                    header = new Table(currentId.getId(), leftValue * rightValue, header);
                    break;
                case DIVIDE:
                    header = new Table(currentId.getId(), leftValue / rightValue, header);
                    break;
                default:
                    System.out.println("[Error]: UnKnow operator");
                    new Bug();
            }
        } else if (exp instanceof Eseq) {
            interpStm(((Eseq) exp).stm);
            interpExp(((Eseq) exp).exp);
            int result = header.getValue();
            header = new Table(currentId.getId(), result, header);
        }
    }

    private void interpExpList(ExpList.T explist) {
        if (explist instanceof ExpList.Pair) {
            // In a ExpList, a Exp May be result a value, so we should
            // generate a Table state for it to store the potential value.
            header = new Table("ExpInExpList" + Long.toString(System.currentTimeMillis()), header);
            interpExp(((ExpList.Pair) explist).exp);
            // After we complete this, we just print it. Because it must
            // be invoked by print() in SLP.
            System.out.print(header.getValue());
            System.out.print(" ");
            interpExpList(((ExpList.Pair) explist).list);
        } else {
            // The same reason above.
            header = new Table("ExpInExpList" + Long.toString(System.currentTimeMillis()), header);
            interpExp(((ExpList.Last) explist).exp);
            System.out.print(header.getValue());
            System.out.print(" ");
        }
    }

    private void interpStm(Stm.T prog) {
        if (prog instanceof Stm.Compound) {
//            new Todo();
            // Added by Yang.Han
            // For a CompoundStm, we should interpret all
            // the stm in it sequentially.
            interpStm(((Stm.Compound) prog).s1);
            interpStm(((Stm.Compound) prog).s2);
        } else if (prog instanceof Stm.Assign) {
//            new Todo();
            // Add By Yang.Han
            // Assign will cause a identifier be initialised
            // or updated. So we try to get it's old state first.
            Table result = Table.lookup(header, ((Stm.Assign) prog).id);
            if (null == result) {
                header = new Table(((Stm.Assign) prog).id, header);
            } else {
                header = new Table(result.getId(), result.getValue(), header);
            }
            interpExp(((Stm.Assign) prog).exp);
        } else if (prog instanceof Stm.Print) {
//            new Todo();
            // Added By Yang.Han
            interpExpList(((Stm.Print) prog).explist);
            // We will print all the args in interExpList, so here we just
            // print a '\n' to end current PrintStm.
            System.out.println();
        } else
            new Bug();
    }

    // ////////////////////////////////////////
    // compile
    HashSet<String> ids;
    StringBuffer buf;

    private void emit(String s) {
        buf.append(s);
    }

    private void compileExp(Exp.T exp) {
        if (exp instanceof Id) {
            Exp.Id e = (Exp.Id) exp;
            String id = e.id;

            emit("\tmovl\t" + id + ", %eax\n");
        } else if (exp instanceof Num) {
            Exp.Num e = (Exp.Num) exp;
            int num = e.num;

            emit("\tmovl\t$" + num + ", %eax\n");
        } else if (exp instanceof Op) {
            Exp.Op e = (Exp.Op) exp;
            Exp.T left = e.left;
            Exp.T right = e.right;
            Exp.OP_T op = e.op;

            switch (op) {
                case ADD:
                    compileExp(left);
                    emit("\tpushl\t%eax\n");
                    compileExp(right);
                    emit("\tpopl\t%edx\n");
                    emit("\taddl\t%edx, %eax\n");
                    break;
                case SUB:
                    compileExp(left);
                    emit("\tpushl\t%eax\n");
                    compileExp(right);
                    emit("\tpopl\t%edx\n");
                    emit("\tsubl\t%eax, %edx\n");
                    emit("\tmovl\t%edx, %eax\n");
                    break;
                case TIMES:
                    compileExp(left);
                    emit("\tpushl\t%eax\n");
                    compileExp(right);
                    emit("\tpopl\t%edx\n");
                    emit("\timul\t%edx\n");
                    break;
                case DIVIDE:
                    compileExp(left);
                    emit("\tpushl\t%eax\n");
                    compileExp(right);
                    emit("\tpopl\t%edx\n");
                    emit("\tmovl\t%eax, %ecx\n");
                    emit("\tmovl\t%edx, %eax\n");
                    emit("\tcltd\n");
                    // Exercise 5
                    // Added by Yang.Han
                    // If ecx is 0, we jump to .skip
                    emit("\tcmp\t$0, %ecx\n");
                    emit("\tjz\t.skip\n");
                    emit("\tdiv\t%ecx\n");
                    // Added by Yang.Han for Exercise 5
                    // Do exit syscall.
                    emit(".skip:\n");
                    emit("\tmov\t$0, %ebx\n");
                    emit("\tmov\t$0x1, %eax\n");
                    emit("\tint\t$0x80\n");
                    break;
                default:
                    new Bug();
            }
        } else if (exp instanceof Eseq) {
            Eseq e = (Eseq) exp;
            Stm.T stm = e.stm;
            Exp.T ee = e.exp;

            compileStm(stm);
            compileExp(ee);
        } else
            new Bug();
    }

    private void compileExpList(ExpList.T explist) {
        if (explist instanceof ExpList.Pair) {
            ExpList.Pair pair = (ExpList.Pair) explist;
            Exp.T exp = pair.exp;
            ExpList.T list = pair.list;

            compileExp(exp);
            emit("\tpushl\t%eax\n");
            emit("\tpushl\t$slp_format\n");
            emit("\tcall\tprintf\n");
            emit("\taddl\t$4, %esp\n");
            compileExpList(list);
        } else if (explist instanceof ExpList.Last) {
            ExpList.Last last = (ExpList.Last) explist;
            Exp.T exp = last.exp;

            compileExp(exp);
            emit("\tpushl\t%eax\n");
            emit("\tpushl\t$slp_format\n");
            emit("\tcall\tprintf\n");
            emit("\taddl\t$4, %esp\n");
        } else
            new Bug();
    }

    private void compileStm(Stm.T prog) {
        if (prog instanceof Stm.Compound) {
            Stm.Compound s = (Stm.Compound) prog;
            Stm.T s1 = s.s1;
            Stm.T s2 = s.s2;

            compileStm(s1);
            compileStm(s2);
        } else if (prog instanceof Stm.Assign) {
            Stm.Assign s = (Stm.Assign) prog;
            String id = s.id;
            Exp.T exp = s.exp;

            ids.add(id);
            compileExp(exp);
            emit("\tmovl\t%eax, " + id + "\n");
        } else if (prog instanceof Stm.Print) {
            Stm.Print s = (Stm.Print) prog;
            ExpList.T explist = s.explist;

            compileExpList(explist);
            emit("\tpushl\t$newline\n");
            emit("\tcall\tprintf\n");
            emit("\taddl\t$4, %esp\n");
        } else
            new Bug();
    }

    // ////////////////////////////////////////
    public void doit(Stm.T prog) {
        // return the maximum number of arguments
        if (Control.ConSlp.action == Control.ConSlp.T.ARGS) {
            int numArgs = maxArgsStm(prog);
            System.out.println(numArgs);
        }

        // interpret a given program
        if (Control.ConSlp.action == Control.ConSlp.T.INTERP) {
            interpStm(prog);
        }

        // compile a given SLP program to x86
        if (Control.ConSlp.action == Control.ConSlp.T.COMPILE) {
            ids = new HashSet<String>();
            buf = new StringBuffer();

            compileStm(prog);
            try {
                // FileOutputStream out = new FileOutputStream();
                FileWriter writer = new FileWriter("slp_gen.s");
                writer.write("// Automatically generated by the Tiger compiler, do NOT edit.\n\n");
                writer.write("\t.data\n");
                writer.write("slp_format:\n");
                writer.write("\t.string \"%d \"\n");
                writer.write("newline:\n");
                writer.write("\t.string \"\\n\"\n");
                for (String s : this.ids) {
                    writer.write(s + ":\n");
                    writer.write("\t.int 0\n");
                }
                writer.write("\n\n\t.text\n");
                writer.write("\t.globl main\n");
                writer.write("main:\n");
                writer.write("\tpushl\t%ebp\n");
                writer.write("\tmovl\t%esp, %ebp\n");
                writer.write(buf.toString());
                writer.write("\tleave\n\tret\n\n");
                writer.close();
                Process child = Runtime.getRuntime().exec("gcc -m32 slp_gen.s");
                child.waitFor();
                if (!Control.ConSlp.keepasm)
                    Runtime.getRuntime().exec("rm -rf slp_gen.s");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
            // System.out.println(buf.toString());
        }
    }
}
