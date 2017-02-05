# Lab1

## PartA SLP

### Exercise 1

What the program will output is:
```cpp
8 7
80
```

### Exercise 2

**A\.** slp.java realise the SLP Syntax by implement some java class to represent Exp, ExpList and Stm. It divide the syntax into three categories: Exp, ExpList and Stm. It first implement Exp, in Exp it implement the *id* and *num*, then use them and the Stm that will be implemented in the later and the Exp itself to conduct Exp recursely. In one word, we implement the "atom" syntax elements, then used them to conduct "non-atom" syntax elements.

**B\.** In fact the sample.java is a sequence that represent the program, but how it represent the program followed the SLP syntax:
```cpp
Stm -> StmA; StmB
StmA -> AssignStm
StmB -> StmB1; StmB2
StmB1 -> (StmB11, ExpB)
StmB11 -> print(ExpListB)
ExplistB -> ExpB2, ExpListB1
ExpB2 -> a
ExpListB1 -> ExpB3
ExpB3 -> a - 1
ExpB -> ExpB1 Binop ExpB11
ExpB1 -> 10
Binop -> *
ExpB11 -> a
stmB2 -> print(ExpListB2)
ExpListB2 -> ExpB21
ExpB21 -> b
```

*Above may have error because it a little complex to write them manually..... Read the code, you will understand it\.*

### Exercise 3

The Code is here, and I have added comments in code.
```java
    // maximum number of args

    private int maxArgsExp(Exp.T exp) {
        // Added by Yang.Han
        // May be EseqExp
        if (exp instanceof Exp.Eseq) {
            int n1 = maxArgsStm(((Exp.Eseq) exp).stm);
            int n2 = maxArgsExp(((Exp.Eseq) exp).exp);
            return n1 >= n2 ? n1 : n2;
        // May be OpExp
        } else if (exp instanceof Exp.Op) {
            int n1 = maxArgsExp(((Exp.Op)exp).left);
            int n2 = maxArgsExp(((Op) exp).right);
            return n1 >= n2 ? n1 : n2;
        // Other Exp won't have PrintStm
        } else {
            return -1;
        }
    }

    // added by Yang.Han
    // I think ExpList should be processed in a single method.
    private int maxArgsExpList(ExpList.T explist) {
        // May be a Pair
        if (explist instanceof ExpList.Pair) {
            int n1, n2;
            n1 = maxArgsExp(((ExpList.Pair) explist).exp);
            n2 = maxArgsExpList(((ExpList.Pair) explist).list);
            return n1 >= n2 ? n1 : n2;
        // May be Last
        } else {
            return maxArgsExp(((ExpList.Last)explist).exp);
        }
    }

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
            int n = 1;
            int n1, n2, max, maxtmp;
            max = 0;
            ExpList.T expListTmp = ((Stm.Print) stm).explist;
            while( expListTmp instanceof ExpList.Pair) {
                n ++;
                n1 = maxArgsExp(((ExpList.Pair) expListTmp).exp);
                n2 = maxArgsExpList(((ExpList.Pair) expListTmp).list);
                maxtmp = n1 >= n2 ? n1 : n2;
                max = max >= maxtmp ? max : maxtmp;
                expListTmp = ((ExpList.Pair) expListTmp).list;
            }
            return n >= max ? n : max;
            // Added by Yang.Han end.
        } else
            new Bug();
        return 0;
    }
```

To use functional memory model, I change it like below.
```java
    private int maxArgsExp(Exp.T exp) {
        // Added by Yang.Han
        // May be EseqExp
        if (exp instanceof Exp.Eseq) {
            int n1 = maxArgsStm(((Exp.Eseq) exp).stm);
            int n2 = maxArgsExp(((Exp.Eseq) exp).exp);
            return n1 >= n2 ? n1 : n2;
        // May be OpExp
        } else if (exp instanceof Exp.Op) {
            int n1 = maxArgsExp(((Exp.Op)exp).left);
            int n2 = maxArgsExp(((Op) exp).right);
            return n1 >= n2 ? n1 : n2;
        // Other Exp won't have PrintStm
        } else {
            return -1;
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
            return maxArgsExp(((ExpList.Last)explist).exp);
        }
    }

    // Lab1 Exercise3
    // Added by Yang.Han
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
```

### Exercise 4

This exercise want us to write the interpretor with functional programming mind. The code is too large to paste here. Just view them in slp/Main.java :)\.

**What I want to say is that I use a variable named header in Class Main, and I update it when do the interpretor, it breaks the taget that coding in functional programming mind :(\.** 
*What caused by this is that I will try to refactor it to match the require later\.*

### Exercise 5

This exercise want us to do a elegent exit when the dividend is zero, what is do is add a cmp before do divide operation, if dividend is zero, directlly exit by call syscall "exit"\.

## PartB Lexer

### Exercise 6


## PartC Parser
