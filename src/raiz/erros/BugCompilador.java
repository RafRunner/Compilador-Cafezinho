package src.raiz.erros;

// Estado que nunca deveria acontecer se o compilador estivesse correto, como um nó que chega ao backend sem ter
// passado pela análise semântica. Não é culpa do programa sendo compilado
public class BugCompilador extends RuntimeException {

    public BugCompilador(String message) {
        super("BUG DO COMPILADOR: " + message);
    }
}
