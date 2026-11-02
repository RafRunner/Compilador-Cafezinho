package src.raiz;

record Opcoes(
        String arquivoFonte, String arquivoSaida, boolean debugar,
        boolean imprimirArvore, boolean imprimirCodigo
) {

    static Opcoes ler(String[] args) {
        String fonte = null;
        String saida = "saida.asm";
        boolean debugar = false, arvore = false, codigo = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o" -> {
                    if (++i == args.length) {
                        throw new IllegalArgumentException("-o precisa do nome do arquivo de saída");
                    }
                    saida = args[i];
                    if (!saida.toLowerCase().endsWith(".asm")) {
                        saida += ".asm";
                    }
                }
                case "-d", "--debug" -> debugar = true;
                case "--arvore" -> arvore = true;
                case "--codigo" -> codigo = true;
                default -> {
                    if (args[i].startsWith("-")) {
                        throw new IllegalArgumentException("Opção desconhecida: " + args[i]);
                    }
                    if (fonte != null) {
                        throw new IllegalArgumentException("Só um arquivo fonte por vez");
                    }
                    fonte = args[i];
                }
            }
        }
        if (fonte == null) {
            throw new IllegalArgumentException("Nenhum arquivo fonte informado");
        }
        return new Opcoes(fonte, saida, debugar, arvore, codigo);
    }
}
