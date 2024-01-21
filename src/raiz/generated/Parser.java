//### This file created by BYACC 1.8(/Java extension  1.15)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";



package src.raiz.generated;



//#line 2 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
    import java.io.*;
    import java.util.*;
    import src.raiz.token.*;
    import src.raiz.ast.*;
    import src.raiz.ast.comandos.*;
    import src.raiz.ast.expressoes.*;
    import src.raiz.util.*;
//#line 25 "Parser.java"




public class Parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 500;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//public class ParserVal is defined in ParserVal.java


String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
void val_init()
{
  valstk=new ParserVal[YYSTACKSIZE];
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
void val_push(ParserVal val)
{
  if (valptr>=YYSTACKSIZE)
    return;
  valstk[++valptr]=val;
}
ParserVal val_pop()
{
  if (valptr<0)
    return new ParserVal();
  return valstk[valptr--];
}
void val_drop(int cnt)
{
int ptr;
  ptr=valptr-cnt;
  if (ptr<0)
    return;
  valptr = ptr;
}
ParserVal val_peek(int relative)
{
int ptr;
  ptr=valptr-relative;
  if (ptr<0)
    return new ParserVal();
  return valstk[ptr];
}
final ParserVal dup_yyval(ParserVal val)
{
  ParserVal dup = new ParserVal();
  dup.ival = val.ival;
  dup.dval = val.dval;
  dup.sval = val.sval;
  dup.obj = val.obj;
  return dup;
}
//#### end semantic value section ####
public final static short PROGRAMA=257;
public final static short CAR=258;
public final static short INT=259;
public final static short RETORNE=260;
public final static short LEIA=261;
public final static short ESCREVA=262;
public final static short NOVALINHA=263;
public final static short SE=264;
public final static short ENTAO=265;
public final static short SENAO=266;
public final static short ENQUANTO=267;
public final static short EXECUTE=268;
public final static short OU=269;
public final static short E=270;
public final static short IGUAL=271;
public final static short DIFERENTE=272;
public final static short MENOR=273;
public final static short MAIOR=274;
public final static short MENOR_IGUAL=275;
public final static short MAIOR_IGUAL=276;
public final static short NEGACAO=277;
public final static short TERNARIO=278;
public final static short MAIS=279;
public final static short MENOS=280;
public final static short VEZES=281;
public final static short DIVISAO=282;
public final static short RESTO=283;
public final static short ATRIBUICAO=284;
public final static short VIRGULA=285;
public final static short PONTO_E_VIRGULA=286;
public final static short DOIS_PONTOS=287;
public final static short ABRE_CHAVE=288;
public final static short FECHA_CHAVE=289;
public final static short ABRE_PARENTESES=290;
public final static short FECHA_PARENTESES=291;
public final static short ABRE_COLCHETE=292;
public final static short FECHA_COLCHETE=293;
public final static short STRING_LITERAL=294;
public final static short CARACTERE_LITERAL=295;
public final static short IDENTIFICADOR=296;
public final static short INT_LITERAL=297;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    1,    1,    6,    2,    2,    2,    7,
    8,    8,   24,   24,   24,   24,    4,    4,    5,    5,
    5,    3,    3,    9,    9,   10,   10,   10,   10,   10,
   10,   10,   10,   10,   10,   10,   12,   13,   13,   23,
   23,   14,   14,   15,   15,   16,   16,   16,   17,   17,
   17,   17,   17,   18,   18,   18,   19,   19,   19,   19,
   20,   20,   20,   11,   11,   21,   21,   21,   21,   21,
   21,   21,   21,   22,   22,
};
final static short yylen[] = {                            2,
    2,    5,    8,    4,    0,    2,    3,    6,    0,    4,
    0,    1,    2,    4,    4,    6,    4,    3,    0,    5,
    8,    1,    1,    1,    2,    1,    2,    3,    3,    3,
    3,    2,    6,    8,    6,    1,    1,    1,    3,    1,
    5,    3,    1,    3,    1,    3,    3,    1,    3,    3,
    3,    3,    1,    3,    3,    1,    3,    3,    3,    1,
    2,    2,    1,    4,    1,    4,    3,    4,    1,    1,
    1,    1,    3,    1,    3,
};
final static short yydefred[] = {                         0,
   23,   22,    0,    0,    0,    0,    1,    0,    0,    6,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   12,    0,    0,    4,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   26,   18,    0,   70,   71,    0,   72,
   36,    0,    0,    0,    0,   37,    0,    0,    0,    0,
    0,    0,   60,   63,   38,    0,    7,    0,    0,    0,
    2,    0,    0,    0,    0,    0,    0,    0,   32,    0,
    0,    0,   62,   61,    0,    0,    0,   17,   25,    0,
   27,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   10,    0,
    0,    0,   28,    0,   29,   31,   30,    0,    0,    0,
   73,   67,   74,    0,    0,   39,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   57,   58,   59,
    0,   15,    0,    0,    0,   20,    0,    0,    0,    0,
    0,   66,    0,    0,    8,    0,    3,    0,   64,    0,
    0,   68,   75,   41,   16,    0,    0,   35,   21,    0,
   34,
};
final static short yydgoto[] = {                          3,
    4,   14,    5,   41,   17,    7,   15,   20,   42,   43,
   44,   45,   46,   47,   48,   49,   50,   51,   52,   53,
   54,  114,   55,   21,
};
final static short yysindex[] = {                      -234,
    0,    0,    0, -223, -247, -251,    0, -230, -234,    0,
 -242, -234, -229, -194, -234, -198, -219, -252, -196, -185,
    0, -176, -234,    0, -246,   -7, -177,   18, -165, -152,
 -151, -193, -193,    0,    0,   -7,    0,    0, -205,    0,
    0, -148,  -99, -136, -144,    0, -259, -120, -207, -142,
 -206, -171,    0,    0,    0, -143,    0, -233, -251, -130,
    0, -141, -113, -112, -116, -109, -107, -106,    0,   -7,
   -7, -201,    0,    0, -105,  258,   -7,    0,    0,   -7,
    0,   43,   -7,   43,   43,   43,   43,   43,   43,   43,
   43,   43,   43,   43,   43, -108, -234, -101,    0,  -92,
  -86, -234,    0,   -7,    0,    0,    0,  -98,  -73,   -7,
    0,    0,    0, -256,  -74,    0, -120,  -67, -207, -142,
 -142, -206, -206, -206, -206, -171, -171,    0,    0,    0,
 -130,    0,  -63, -234, -130,    0,  -61,  -22,  -24,  -48,
   -7,    0,    0,   43,    0, -234,    0,  -39,    0,  -99,
  -99,    0,    0,    0,    0, -234,  -17,    0,    0,  -99,
    0,
};
final static short yyrindex[] = {                         1,
    0,    0,    0,    0,    0,    0,    0,  -21, -181,    0,
    0,    3,    0,    0,    1,    0,    0,  -21,    0,    0,
    0,    0,    1,    0,  -21,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  -70,    0,
    0,    0,  -18,    0,    0,    0, -265,  236, -255,  191,
   81,    6,    0,    0,    0,    0,    0,    5,    0,  -21,
    0,    0,    0,    0,   33,    0,  287,    0,    0,    0,
    0,  -19,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0, -181,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  246,    0, -103,  216,
  226,  106,  131,  156,  181,   31,   56,    0,    0,    0,
  -21,    0,   30,    1,  -21,    0,    0,    0,    0,    0,
    0,    0,  -45,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0, -181, -137,    0,    0,    0,
    0,
};
final static short yygindex[] = {                         0,
  -14,  -13,   -9,    7, -100,    0,    0,    0,  301, -133,
  242,  -20,  -69,    0,  263,  262,    9,   82,   37,   51,
  103,    0,  204,  -93,
};
final static int YYTABLESIZE=570;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         16,
   24,  136,   19,  132,   57,   64,  113,   68,   61,   82,
  116,   63,   10,   45,   45,   75,  157,  158,   83,   40,
   40,   40,   45,    1,    2,   40,  161,   40,  141,   45,
   45,   45,   11,    6,  142,   45,    9,   45,   11,   56,
   26,   27,   28,   29,   30,   62,  100,   31,    8,  108,
  109,   97,  155,   18,   11,  159,  115,   32,   98,   12,
   33,   13,  118,   85,   86,   99,   34,   22,    9,   35,
   36,  153,   91,   92,   37,   38,   39,   40,   19,   19,
   19,   19,   19,  137,   76,   19,   77,   19,   76,  140,
  110,   23,   16,  120,  121,   19,   36,   25,   19,   58,
   37,   38,   72,   40,   19,   59,   19,   19,   19,   93,
   94,   95,   19,   19,   19,   19,   60,  145,   65,  147,
   69,  148,   33,   33,   33,   33,   33,  126,  127,   33,
   87,   88,   89,   90,   73,   74,   19,   70,   71,   33,
   78,   81,   33,  128,  129,  130,   16,   80,   33,   84,
   33,   33,   33,   96,   11,  101,   33,   33,   33,   33,
   26,   27,   28,   29,   30,   44,   44,   31,  122,  123,
  124,  125,  102,  103,   44,  104,  105,   32,  106,  107,
   33,   44,   44,   44,  131,  111,   34,   44,    9,   44,
   36,  133,  138,  134,   37,   38,   39,   40,   69,   69,
   69,   69,   69,   69,   69,   69,  135,   69,   69,   69,
   69,   69,   69,   65,   69,   69,   69,  139,  143,  144,
   69,  146,   69,   68,   68,   68,   68,   68,   68,   68,
   68,  149,   68,   68,   68,   68,   68,   68,   64,   68,
   68,   68,  150,  151,  152,   68,  156,   68,  160,   69,
   69,   69,   69,   69,   69,   69,   69,    5,   69,   69,
   69,   69,   69,   69,    9,   69,   69,   69,   66,   32,
   24,   69,   33,   69,   56,   56,   56,   56,   56,   56,
   56,   56,   36,   56,   56,   56,   37,   38,   39,   40,
   56,   56,   56,   11,   32,   13,   56,   33,   56,   54,
   54,   54,   54,   54,   54,   54,   54,   36,   54,   54,
   54,   67,   38,   39,   40,   54,   54,   54,   65,   32,
   14,   54,   33,   54,   55,   55,   55,   55,   55,   55,
   55,   55,   36,   55,   55,   55,   37,   38,   72,   40,
   55,   55,   55,   79,  117,  119,   55,  154,   55,   53,
   53,   53,   53,   53,   53,   53,   53,    0,   53,    0,
    0,    0,    0,    0,    0,   53,   53,   53,    0,    0,
    0,   53,    0,   53,   49,   49,   49,   49,   49,   49,
   49,   49,    0,   49,    0,    0,    0,    0,    0,    0,
   49,   49,   49,    0,    0,    0,   49,    0,   49,   50,
   50,   50,   50,   50,   50,   50,   50,    0,   50,    0,
    0,    0,    0,    0,    0,   50,   50,   50,    0,    0,
    0,   50,    0,   50,   52,   52,   52,   52,   52,   52,
   52,   52,    0,   52,    0,    0,    0,    0,    0,    0,
   52,   52,   52,    0,    0,    0,   52,    0,   52,   51,
   51,   51,   51,   51,   51,   51,   51,    0,   51,   48,
   48,   48,   48,    0,    0,   51,   51,   51,   48,    0,
    0,   51,    0,   51,    0,   48,   48,   48,    0,    0,
    0,   48,    0,   48,   46,   46,   46,   46,    0,    0,
    0,    0,    0,   46,   47,   47,   47,   47,    0,    0,
   46,   46,   46,   47,   43,    0,   46,    0,   46,    0,
   47,   47,   47,   43,   42,    0,   47,    0,   47,    0,
   43,   43,   43,   42,    0,    0,   43,    0,   43,    0,
   42,   42,   42,    0,   32,    0,   42,   33,   42,    0,
    0,    0,    0,    0,    0,    0,    0,   36,  112,    0,
    0,   37,   38,   39,   40,   70,   70,   70,   70,   70,
   70,   70,   70,    0,   70,   70,   70,   70,   70,   70,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                          9,
   15,  102,   12,   97,   18,   26,   76,   28,   23,  269,
   80,   25,    6,  269,  270,   36,  150,  151,  278,  285,
  286,  287,  278,  258,  259,  291,  160,  293,  285,  285,
  286,  287,  285,  257,  291,  291,  288,  293,  285,  292,
  260,  261,  262,  263,  264,  292,   60,  267,  296,   70,
   71,  285,  146,  296,  285,  156,   77,  277,  292,  290,
  280,  292,   83,  271,  272,   59,  286,  297,  288,  289,
  290,  141,  279,  280,  294,  295,  296,  297,  260,  261,
  262,  263,  264,  104,  290,  267,  292,   97,  290,  110,
  292,  286,  102,   85,   86,  277,  290,  296,  280,  296,
  294,  295,  296,  297,  286,  291,  288,  289,  290,  281,
  282,  283,  294,  295,  296,  297,  293,  131,  296,  134,
  286,  135,  260,  261,  262,  263,  264,   91,   92,  267,
  273,  274,  275,  276,   32,   33,  146,  290,  290,  277,
  289,  286,  280,   93,   94,   95,  156,  284,  286,  270,
  288,  289,  290,  297,  285,  297,  294,  295,  296,  297,
  260,  261,  262,  263,  264,  269,  270,  267,   87,   88,
   89,   90,  286,  286,  278,  292,  286,  277,  286,  286,
  280,  285,  286,  287,  293,  291,  286,  291,  288,  293,
  290,  293,  291,  286,  294,  295,  296,  297,  269,  270,
  271,  272,  273,  274,  275,  276,  293,  278,  279,  280,
  281,  282,  283,  284,  285,  286,  287,  291,  293,  287,
  291,  285,  293,  269,  270,  271,  272,  273,  274,  275,
  276,  293,  278,  279,  280,  281,  282,  283,  284,  285,
  286,  287,  265,  268,  293,  291,  286,  293,  266,  269,
  270,  271,  272,  273,  274,  275,  276,  257,  278,  279,
  280,  281,  282,  283,  286,  285,  286,  287,   27,  277,
  289,  291,  280,  293,  269,  270,  271,  272,  273,  274,
  275,  276,  290,  278,  279,  280,  294,  295,  296,  297,
  285,  286,  287,  291,  277,  291,  291,  280,  293,  269,
  270,  271,  272,  273,  274,  275,  276,  290,  278,  279,
  280,  294,  295,  296,  297,  285,  286,  287,  286,  277,
  291,  291,  280,  293,  269,  270,  271,  272,  273,  274,
  275,  276,  290,  278,  279,  280,  294,  295,  296,  297,
  285,  286,  287,   43,   82,   84,  291,  144,  293,  269,
  270,  271,  272,  273,  274,  275,  276,   -1,  278,   -1,
   -1,   -1,   -1,   -1,   -1,  285,  286,  287,   -1,   -1,
   -1,  291,   -1,  293,  269,  270,  271,  272,  273,  274,
  275,  276,   -1,  278,   -1,   -1,   -1,   -1,   -1,   -1,
  285,  286,  287,   -1,   -1,   -1,  291,   -1,  293,  269,
  270,  271,  272,  273,  274,  275,  276,   -1,  278,   -1,
   -1,   -1,   -1,   -1,   -1,  285,  286,  287,   -1,   -1,
   -1,  291,   -1,  293,  269,  270,  271,  272,  273,  274,
  275,  276,   -1,  278,   -1,   -1,   -1,   -1,   -1,   -1,
  285,  286,  287,   -1,   -1,   -1,  291,   -1,  293,  269,
  270,  271,  272,  273,  274,  275,  276,   -1,  278,  269,
  270,  271,  272,   -1,   -1,  285,  286,  287,  278,   -1,
   -1,  291,   -1,  293,   -1,  285,  286,  287,   -1,   -1,
   -1,  291,   -1,  293,  269,  270,  271,  272,   -1,   -1,
   -1,   -1,   -1,  278,  269,  270,  271,  272,   -1,   -1,
  285,  286,  287,  278,  269,   -1,  291,   -1,  293,   -1,
  285,  286,  287,  278,  269,   -1,  291,   -1,  293,   -1,
  285,  286,  287,  278,   -1,   -1,  291,   -1,  293,   -1,
  285,  286,  287,   -1,  277,   -1,  291,  280,  293,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  290,  291,   -1,
   -1,  294,  295,  296,  297,  269,  270,  271,  272,  273,
  274,  275,  276,   -1,  278,  279,  280,  281,  282,  283,
};
}
final static short YYFINAL=3;
final static short YYMAXTOKEN=297;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"PROGRAMA","CAR","INT","RETORNE","LEIA","ESCREVA","NOVALINHA",
"SE","ENTAO","SENAO","ENQUANTO","EXECUTE","OU","E","IGUAL","DIFERENTE","MENOR",
"MAIOR","MENOR_IGUAL","MAIOR_IGUAL","NEGACAO","TERNARIO","MAIS","MENOS","VEZES",
"DIVISAO","RESTO","ATRIBUICAO","VIRGULA","PONTO_E_VIRGULA","DOIS_PONTOS",
"ABRE_CHAVE","FECHA_CHAVE","ABRE_PARENTESES","FECHA_PARENTESES","ABRE_COLCHETE",
"FECHA_COLCHETE","STRING_LITERAL","CARACTERE_LITERAL","IDENTIFICADOR",
"INT_LITERAL",
};
final static String yyrule[] = {
"$accept : Programa",
"Programa : DeclFuncVar DeclProg",
"DeclFuncVar : Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA DeclFuncVar",
"DeclFuncVar : Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA DeclFuncVar",
"DeclFuncVar : Tipo IDENTIFICADOR DeclFunc DeclFuncVar",
"DeclFuncVar :",
"DeclProg : PROGRAMA Bloco",
"DeclVar : VIRGULA IDENTIFICADOR DeclVar",
"DeclVar : VIRGULA IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar",
"DeclVar :",
"DeclFunc : ABRE_PARENTESES ListaParametros FECHA_PARENTESES Bloco",
"ListaParametros :",
"ListaParametros : ListaParametrosCont",
"ListaParametrosCont : Tipo IDENTIFICADOR",
"ListaParametrosCont : Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE",
"ListaParametrosCont : Tipo IDENTIFICADOR VIRGULA ListaParametrosCont",
"ListaParametrosCont : Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE VIRGULA ListaParametrosCont",
"Bloco : ABRE_CHAVE ListaDeclVar ListaComando FECHA_CHAVE",
"Bloco : ABRE_CHAVE ListaDeclVar FECHA_CHAVE",
"ListaDeclVar :",
"ListaDeclVar : Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA ListaDeclVar",
"ListaDeclVar : Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA ListaDeclVar",
"Tipo : INT",
"Tipo : CAR",
"ListaComando : Comando",
"ListaComando : Comando ListaComando",
"Comando : PONTO_E_VIRGULA",
"Comando : Expr PONTO_E_VIRGULA",
"Comando : RETORNE Expr PONTO_E_VIRGULA",
"Comando : LEIA LValueExpr PONTO_E_VIRGULA",
"Comando : ESCREVA Expr PONTO_E_VIRGULA",
"Comando : ESCREVA STRING_LITERAL PONTO_E_VIRGULA",
"Comando : NOVALINHA PONTO_E_VIRGULA",
"Comando : SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando",
"Comando : SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando SENAO Comando",
"Comando : ENQUANTO ABRE_PARENTESES Expr FECHA_PARENTESES EXECUTE Comando",
"Comando : Bloco",
"Expr : AssignExpr",
"AssignExpr : CondExpr",
"AssignExpr : LValueExpr ATRIBUICAO AssignExpr",
"CondExpr : OrExpr",
"CondExpr : OrExpr TERNARIO Expr DOIS_PONTOS CondExpr",
"OrExpr : OrExpr OU AndExpr",
"OrExpr : AndExpr",
"AndExpr : AndExpr E EqExpr",
"AndExpr : EqExpr",
"EqExpr : EqExpr IGUAL DesigExpr",
"EqExpr : EqExpr DIFERENTE DesigExpr",
"EqExpr : DesigExpr",
"DesigExpr : DesigExpr MENOR AddExpr",
"DesigExpr : DesigExpr MAIOR AddExpr",
"DesigExpr : DesigExpr MAIOR_IGUAL AddExpr",
"DesigExpr : DesigExpr MENOR_IGUAL AddExpr",
"DesigExpr : AddExpr",
"AddExpr : AddExpr MAIS MulExpr",
"AddExpr : AddExpr MENOS MulExpr",
"AddExpr : MulExpr",
"MulExpr : MulExpr VEZES UnExpr",
"MulExpr : MulExpr DIVISAO UnExpr",
"MulExpr : MulExpr RESTO UnExpr",
"MulExpr : UnExpr",
"UnExpr : MENOS PrimExpr",
"UnExpr : NEGACAO PrimExpr",
"UnExpr : PrimExpr",
"LValueExpr : IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE",
"LValueExpr : IDENTIFICADOR",
"PrimExpr : IDENTIFICADOR ABRE_PARENTESES ListExpr FECHA_PARENTESES",
"PrimExpr : IDENTIFICADOR ABRE_PARENTESES FECHA_PARENTESES",
"PrimExpr : IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE",
"PrimExpr : IDENTIFICADOR",
"PrimExpr : STRING_LITERAL",
"PrimExpr : CARACTERE_LITERAL",
"PrimExpr : INT_LITERAL",
"PrimExpr : ABRE_PARENTESES Expr FECHA_PARENTESES",
"ListExpr : AssignExpr",
"ListExpr : ListExpr VIRGULA AssignExpr",
};

//#line 494 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"

private Lexer lexer;
private Programa programa;
private boolean debugInterno = false;

private List<ParametroFuncao> parametrosAtuais = new LinkedList<>();

private void debugar(Object objeto) {
    if (debugInterno) {
        System.out.println(objeto);
    }
}

private DeclaracaoDeVariavel montaDeclaracao(Variavel variavel, List<Variavel> resto) {
    List<Variavel> variaveis = new LinkedList<>();
    variaveis.add(variavel);
    variaveis.addAll(resto);

    for (Variavel v : resto) {
        v.setTipo(variavel.getTipo());
    }

    DeclaracaoDeVariavel declaracao = new DeclaracaoDeVariavel(variavel.getTipo(), variaveis);

    return declaracao;
}

private String getLexema(Object token) {
    return ((Token) token).getLexema();
}

private void yyerror(String mensagemErro) {
    if ("syntax error".equals(mensagemErro)) {
        mensagemErro = "Erro de sintaxe";
    }
    throw new RuntimeException(AstUtil.montaMensagemErro(mensagemErro, (Token) yylval.obj));
}

private Integer tokenParaInt(Token token) {
    return Integer.parseInt(token.getLexema());
}

// Transforma um token em um número. Como vetores tem que ter tamanho 1 no mínimo, 0 aqui é um erro semântico
private Integer tokenParaTamanhoVetor(Token numero, Token variavel) {
    Integer tamanho = tokenParaInt(numero);
    if (tamanho == 0) {
        this.programa.reportaErroSemantico("Array " + variavel.getLexema() + " não pode ter tamanho 0", variavel);
    }

    return tamanho;
}

// Função que traduz o Token do lexer para um valor int do BYACC/J
private int yylex() {
    try {
        Token proximoToken = lexer.yylex();
        debugar(proximoToken);
        if (proximoToken.getTipo() != TipoToken.EOF) {
            yylval = new ParserVal(proximoToken);

            // Os valores de 0 a 256 são reservados, por isso adicionamos 257
            return proximoToken.getTipo().ordinal() + 257;
        } else {
            return 0; // EOF
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

public Programa getPrograma() {
    return programa;
}

public void analisar(Reader leitor) {
    analisar(leitor, false);
}

public void analisar(Reader leitor, boolean debugInterno) {
    programa = new Programa(); // Cria um novo objeto de programa
    lexer = new Lexer(leitor); // Cria Lexer para ler o arquivo
    this.debugInterno = debugInterno;
    yyparse(); // Cria a AST fazendo análise sintática e semântica

    // Se existem erros semânticos, reportamos e abortamos a geração do programa
    if (!programa.getErrosSemanticos().isEmpty()) {
        StringBuilder sb = new StringBuilder("Erros semânticos foram identificados na análise:\n");

        for (String mensagem : programa.getErrosSemanticos()) {
            sb.append(mensagem).append("\n");
        }

        throw new RuntimeException(sb.toString());
    }
}

//#line 553 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  val_push(yylval);     //save empty value
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 37 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        /* Última regra a ser derivada. Termina de montar o programa.*/
        debugar("Programa derivado com sucesso");
        this.programa.setDeclaracaoFuncoesEVariaveis((DeclaracaoFuncoesEVariaveis) val_peek(1).obj);
        this.programa.setBlocoPrograma((BlocoPrograma) val_peek(0).obj);
    }
break;
case 2:
//#line 46 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        TipoVariavelNo tipo = (TipoVariavelNo) val_peek(4).obj;
        Token identificador = (Token) val_peek(3).obj;

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável " + identificador.getLexema());

        List<Variavel> resto = (List<Variavel>) val_peek(2).obj;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) val_peek(0).obj;

        Variavel variavel = new Variavel(tipo, identificador, null);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, montaDeclaracao(variavel, resto));

        yyval.obj = outrasDeclaracoes;
    }
break;
case 3:
//#line 60 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        TipoVariavelNo tipo = (TipoVariavelNo) val_peek(7).obj;
        Token identificador = (Token) val_peek(6).obj;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) val_peek(4).obj, (Token) val_peek(6).obj);

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável vetor "
            + identificador.getLexema() + " tamanho: " + tamanhoVetor);

        List<Variavel> resto = (List<Variavel>) val_peek(2).obj;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) val_peek(0).obj;

        Variavel variavel = new Variavel(tipo, identificador, tamanhoVetor);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, montaDeclaracao(variavel, resto));

        yyval.obj = outrasDeclaracoes;
    }
break;
case 4:
//#line 76 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        List<ParametroFuncao> parametros = (List<ParametroFuncao>) ((Object[]) val_peek(1).obj)[0];
        BlocoDeclaracoes corpo = (BlocoDeclaracoes) ((Object[]) val_peek(1).obj)[1];

        DeclaracaoFuncao declaracaoFuncao = new DeclaracaoFuncao((Token) val_peek(2).obj, (TipoVariavelNo) val_peek(3).obj, corpo, parametros);
        debugar("Declaracao de função " + declaracaoFuncao.getNome() + " " + declaracaoFuncao.getTipoRetorno());

        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) val_peek(0).obj;
        outrasDeclaracoes.getDeclaracoesDeFuncoes().add(0, declaracaoFuncao);

        yyval.obj = outrasDeclaracoes;
    }
break;
case 5:
//#line 88 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ yyval.obj = new DeclaracaoFuncoesEVariaveis(); }
break;
case 6:
//#line 92 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Bloco do programa derivado");
        yyval.obj = new BlocoPrograma((Token) val_peek(1).obj, (BlocoDeclaracoes) val_peek(0).obj);
    }
break;
case 7:
//#line 99 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        Token identificador = (Token) val_peek(1).obj;
        List<Variavel> variaveis = (List<Variavel>) val_peek(0).obj;

        debugar("Declaração de variável " + identificador.getLexema());

        Variavel variavel = new Variavel(null, identificador, null);
        variaveis.add(0, variavel);

        yyval.obj = variaveis;
    }
break;
case 8:
//#line 110 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        Token identificador = (Token) val_peek(4).obj;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) val_peek(2).obj, identificador);
        List<Variavel> variaveis = (List<Variavel>) val_peek(0).obj;

        debugar("Declaração de variável vetor " + identificador.getLexema() + " tamanho: " + tamanhoVetor);

        Variavel variavel = new Variavel(null, (Token) val_peek(4).obj, tamanhoVetor);
        variaveis.add(0, variavel);

        yyval.obj = variaveis;
    }
break;
case 9:
//#line 122 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ yyval.obj = new LinkedList<Variavel>(); }
break;
case 10:
//#line 126 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Declaraco de função derivado");
        yyval.obj = new Object[]{val_peek(2).obj, val_peek(0).obj};
     }
break;
case 11:
//#line 133 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Lista de parâmetros vazia.");
        yyval.obj = new LinkedList<>();
    }
break;
case 12:
//#line 137 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Lista de parâmetros finalizada. Tamanho: " + parametrosAtuais.size());
        yyval.obj = new LinkedList<>(parametrosAtuais);
        parametrosAtuais = new LinkedList<>();
    }
break;
case 13:
//#line 145 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Útimo parâmetro " + getLexema(val_peek(0).obj) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) val_peek(0).obj, (TipoVariavelNo) val_peek(1).obj, false));
    }
break;
case 14:
//#line 149 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Útimo parâmetro vetor " + getLexema(val_peek(2).obj) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) val_peek(2).obj, (TipoVariavelNo) val_peek(3).obj, true));
    }
break;
case 15:
//#line 153 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Parâmetro " + getLexema(val_peek(2).obj) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) val_peek(2).obj, (TipoVariavelNo) val_peek(3).obj, false));
    }
break;
case 16:
//#line 157 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Parâmetro vetor " + getLexema(val_peek(4).obj) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) val_peek(4).obj, (TipoVariavelNo) val_peek(5).obj, true));
    }
break;
case 17:
//#line 164 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Bloco com comandos derivado");
        BlocoDeclaracoes bloco = new BlocoDeclaracoes((Token) val_peek(3).obj);
        bloco.getDeclaracoes().add((Declaracao) val_peek(2).obj);
        bloco.getDeclaracoes().addAll((List<Declaracao>) val_peek(1).obj);

        yyval.obj = bloco;
    }
break;
case 18:
//#line 172 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ 
        debugar("Bloco somente com variáveis derivado");
        BlocoDeclaracoes bloco = new BlocoDeclaracoes((Token) val_peek(2).obj);
        bloco.getDeclaracoes().add((Declaracao) val_peek(1).obj);

        yyval.obj = bloco;
    }
break;
case 19:
//#line 182 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        yyval.obj = new DeclaracaoVariavelEmBloco();
      }
break;
case 20:
//#line 185 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        Variavel variavel = new Variavel((TipoVariavelNo) val_peek(4).obj, (Token) val_peek(3).obj, null);
        debugar("Declaração de variáveis em bloco do tipo " + variavel.getTipo() + " começando com variável " + variavel.getNome());

        List<Variavel> outrasVariaveis = (List<Variavel>) val_peek(2).obj;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = (DeclaracaoVariavelEmBloco) val_peek(0).obj;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        yyval.obj = declaracaoVariavelEmBloco;
    }
break;
case 21:
//#line 202 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        Token tokenVariavel = (Token) val_peek(6).obj;
        Variavel variavel = new Variavel((TipoVariavelNo) val_peek(7).obj, tokenVariavel, tokenParaTamanhoVetor((Token) val_peek(4).obj, tokenVariavel));

        debugar("Declaração de variáveis em bloco do tipo " + variavel.getTipo() + " começando com variável vetor "
            + variavel.getNome() + " tamanho: " + variavel.getTamanhoVetor());

        List<Variavel> outrasVariaveis = (List<Variavel>) val_peek(2).obj;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = (DeclaracaoVariavelEmBloco) val_peek(0).obj;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        yyval.obj = declaracaoVariavelEmBloco;
    }
break;
case 22:
//#line 225 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ debugar("Tipo: " + getLexema(val_peek(0).obj)); yyval.obj = new TipoVariavelNo((Token) val_peek(0).obj, TipoVariavel.INTEIRO); }
break;
case 23:
//#line 226 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ debugar("Tipo: " + getLexema(val_peek(0).obj)); yyval.obj = new TipoVariavelNo((Token) val_peek(0).obj, TipoVariavel.CARACTERE); }
break;
case 24:
//#line 230 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Primeiro comando derivado");
        List<Comando> comandos = new LinkedList<>();
        comandos.add(0, (Comando) val_peek(0).obj);
        yyval.obj = comandos;
    }
break;
case 25:
//#line 236 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando derivado");
        List<Comando> comandos = (List<Comando>) val_peek(0).obj;
        comandos.add(0, (Comando) val_peek(1).obj);
        yyval.obj = comandos;
    }
break;
case 26:
//#line 245 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando vazio ';'");
        /* Comando vazio*/
        yyval.obj = null;
    }
break;
case 27:
//#line 250 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando com expressão");
        Expressao expressao = (Expressao) val_peek(1).obj;
        yyval.obj = new ComandoComExpressao(expressao.getToken(), expressao);
    }
break;
case 28:
//#line 255 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando de retorno");
        Expressao expressao = (Expressao) val_peek(1).obj;
        yyval.obj = new ComandoRetorno((Token) val_peek(2).obj, expressao);
    }
break;
case 29:
//#line 260 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando leia");
        yyval.obj = new ComandoLeia((Token) val_peek(2).obj, (ExpressaoIdentificador) val_peek(1).obj);
    }
break;
case 30:
//#line 264 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando escreva");
        yyval.obj = new ComandoEscreva((Token) val_peek(2).obj, (Expressao) val_peek(1).obj);
    }
break;
case 31:
//#line 268 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando escreva String " + getLexema(val_peek(1).obj));
        ExpressaoStringLiteral expressao = new ExpressaoStringLiteral((Token) val_peek(1).obj);

        yyval.obj = new ComandoEscreva((Token) val_peek(2).obj, expressao);
    }
break;
case 32:
//#line 274 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando novalinha");
        yyval.obj = new ComandoNovalinha((Token) val_peek(1).obj);
    }
break;
case 33:
//#line 278 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando se simples");
        yyval.obj = new ComandoSe((Token) val_peek(5).obj, (Expressao) val_peek(3).obj, (Comando) val_peek(0).obj);
    }
break;
case 34:
//#line 282 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando se senao");
        yyval.obj = new ComandoSe((Token) val_peek(7).obj, (Expressao) val_peek(5).obj, (Comando) val_peek(2).obj, (Comando) val_peek(0).obj);
    }
break;
case 35:
//#line 286 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando enquanto");
        yyval.obj = new ComandoEnquanto((Token) val_peek(5).obj, (Expressao) val_peek(3).obj, (Comando) val_peek(0).obj);
    }
break;
case 36:
//#line 290 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando bloco");
        BlocoDeclaracoes bloco = (BlocoDeclaracoes) val_peek(0).obj;
        yyval.obj = new ComandoBloco(bloco.getToken(), bloco);
    }
break;
case 37:
//#line 298 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 38:
//#line 305 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão CondExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 39:
//#line 309 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de atribuição derivada");
        ExpressaoIdentificador identificador = (ExpressaoIdentificador) val_peek(2).obj;
        yyval.obj = new ExpressaoAtribuicao((Token) val_peek(1).obj, identificador, (Expressao) val_peek(0).obj);
    }
break;
case 40:
//#line 317 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ debugar("Expressão OrExpr derivada"); }
break;
case 41:
//#line 318 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão ou ternária derivada");
        yyval.obj = new ExpressaoTernaria((Token) val_peek(3).obj, (Expressao) val_peek(4).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 42:
//#line 325 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão ou derivada");
        yyval.obj = new ExpressaoOu((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 43:
//#line 329 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão e derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 44:
//#line 336 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão e derivada");
        yyval.obj = new ExpressaoE((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 45:
//#line 340 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão EqExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 46:
//#line 347 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão igual derivada");
        yyval.obj = new ExpressaoIgual((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 47:
//#line 351 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão diferente derivada");
        yyval.obj = new ExpressaoDiferente((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 48:
//#line 355 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão DesigExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 49:
//#line 362 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão menor derivada");
        yyval.obj = new ExpressaoMenor((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 50:
//#line 366 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão maior derivada");
        yyval.obj = new ExpressaoMaior((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 51:
//#line 370 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão maior igual derivada");
        yyval.obj = new ExpressaoMaiorIgual((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 52:
//#line 374 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão menor igual derivada");
        yyval.obj = new ExpressaoMenorIgual((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 53:
//#line 378 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão AddExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 54:
//#line 385 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão mais derivada");
        yyval.obj = new ExpressaoMais((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 55:
//#line 389 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão menos derivada");
        yyval.obj = new ExpressaoMenos((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 56:
//#line 393 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão MulExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 57:
//#line 400 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão vezes derivada");
        yyval.obj = new ExpressaoVezes((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 58:
//#line 404 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão divisão derivada");
        yyval.obj = new ExpressaoDivisao((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 59:
//#line 408 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão resto derivada");
        yyval.obj = new ExpressaoResto((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 60:
//#line 412 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão UnExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 61:
//#line 419 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão menos unária derivada");
        yyval.obj = new ExpressaoNegativo((Token) val_peek(1).obj, (Expressao) val_peek(0).obj);
    }
break;
case 62:
//#line 423 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão negação derivada");
        yyval.obj = new ExpressaoNegacao((Token) val_peek(1).obj, (Expressao) val_peek(0).obj);
    }
break;
case 63:
//#line 427 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão PrimExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 64:
//#line 434 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de indexação no vetor " + getLexema(val_peek(3).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(3).obj, (Expressao) val_peek(1).obj);
    }
break;
case 65:
//#line 438 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de identificador(LValueExpr) " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(0).obj, null);
    }
break;
case 66:
//#line 445 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de chamada da função " + getLexema(val_peek(3).obj));
        yyval.obj = new ExpressaoChamadaFuncao((Token) val_peek(3).obj, (LinkedList<Expressao>) val_peek(1).obj);
    }
break;
case 67:
//#line 449 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de chamada da função sem argumentos " + getLexema(val_peek(2).obj));
        yyval.obj = new ExpressaoChamadaFuncao((Token) val_peek(2).obj, new LinkedList<>());
    }
break;
case 68:
//#line 453 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de indexação no vetor " + getLexema(val_peek(3).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(3).obj, (Expressao) val_peek(1).obj);
    }
break;
case 69:
//#line 457 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de identificador(PrimExpr) " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(0).obj, null);
    }
break;
case 70:
//#line 461 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Literal string " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoStringLiteral((Token) val_peek(0).obj);
    }
break;
case 71:
//#line 465 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Literal caractere " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoCaractereLiteral((Token) val_peek(0).obj);
    }
break;
case 72:
//#line 469 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Literal inteiro " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoInteiroLiteral((Token) val_peek(0).obj);
    }
break;
case 73:
//#line 473 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão entre parênteses derivada");
        yyval.obj = new ExpressaoEntreParenteses((Token) val_peek(2).obj, (Expressao) val_peek(1).obj);
    }
break;
case 74:
//#line 480 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Lista de expressões iniciada.");
        LinkedList<Expressao> parametrosChamadaAtuais = new LinkedList<>();
        parametrosChamadaAtuais.add((Expressao) val_peek(0).obj);
        yyval.obj = parametrosChamadaAtuais;
    }
break;
case 75:
//#line 486 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Mais um parâmetro em lista de expressão declarado");
        LinkedList<Expressao> parametrosChamadaAtuais = (LinkedList<Expressao>) val_peek(2).obj;
        parametrosChamadaAtuais.add((Expressao) val_peek(0).obj);
    }
break;
//#line 1309 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
/**
 * A default run method, used for operating this parser
 * object in the background.  It is intended for extending Thread
 * or implementing Runnable.  Turn off with -Jnorun .
 */
public void run()
{
  yyparse();
}
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public Parser()
{
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public Parser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
