public class Maquina {
    
    // Atributos
    private String nome;
    private boolean ligada;
    private double capacidadeMaxima;

    // Construtor
    public Maquina(String nome, double capacidadeMaxima) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ligada = false; // Toda máquina nasce desligada por segurança
    }

    // Métodos
    public void ligar(){
        this.ligada = true;
    }

    public void desligar(){
        this.ligada = false;
    }

    public void processar( MateriaPrima materiaPrima, double demanda) {
        if (!this.ligada){
            System.out.println("[ERRO] A máquina " + this.nome + " está desligada.");
            return;
        } 
        if ( demanda > this.capacidadeMaxima){
            System.out.println("[ERRO] Sobrecarga! A demanda (" + demanda + ") excede a capacidade máxima (" + this.capacidadeMaxima + ") da máquina.");
            return;
        }
        if (materiaPrima.verificarDisponibilidade(demanda)) {
            materiaPrima.consumir(demanda);
            System.out.println("[OK] Processamento concluído. Foram consumidos " + demanda + " de " + materiaPrima.getNome() + ".");
        } else {
            System.out.println("[ERRO] Estoque insuficiente de " + materiaPrima.getNome() + " para iniciar o processamento.");
        }
    }

    // Métodos de acesso (getters)
    public String getNome() {
        return this.nome;
    }
    public boolean estaLigada() {
        return this.ligada;
    }
}

