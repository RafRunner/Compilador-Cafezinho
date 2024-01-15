package src.raiz.erros;

public class BugCompilador extends RuntimeException {

    public BugCompilador(String message) {
        super("BUG DO COMPILADOR: " + message);
    }
}
