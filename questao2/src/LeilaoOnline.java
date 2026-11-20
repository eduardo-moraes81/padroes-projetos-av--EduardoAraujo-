package questao2;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

interface Categoria {
    String rotulo();
    RegraComissao regraComissaoPadrao();
}

final class Lance {
    private final Licitante licitante;
    private final BigDecimal valor;
    private final LocalDateTime dataHora;

    Lance(Licitante licitante, BigDecimal valor) {
        this.licitante = licitante;
        this.valor = valor;
        this.dataHora = LocalDateTime.now();
    }

    Licitante licitante() { return licitante; }
    BigDecimal valor() { return valor; }
    LocalDateTime dataHora() { return dataHora; }
}

interface ObservadorLance {
    void aoReceberLance(LoteLeilao lote, Lance lance, int quantidadeLances);
}

interface Licitante {
    String nome();
}

final class Participante implements Licitante, ObservadorLance {
    private final String nome;
    Participante(String nome) { this.nome = nome; }
    @Override public String nome() { return nome; }

    @Override
    public void aoReceberLance(LoteLeilao lote, Lance lance, int quantidadeLances) {
        System.out.println("[Participante " + nome + "] " + lote.nome() + " recebeu lance de R$ "
                + TextoMonetario.formatar(lance.valor()) + ".");
    }
}

final class PainelLeiloeiro implements ObservadorLance {
    private final String nome;
    PainelLeiloeiro(String nome) { this.nome = nome; }

    @Override
    public void aoReceberLance(LoteLeilao lote, Lance lance, int quantidadeLances) {
        System.out.println("[Painel " + nome + "] " + lote.nome() + " | maior lance: R$ "
                + TextoMonetario.formatar(lance.valor()) + " | lances: " + quantidadeLances);
    }
}

final class AuditorLances implements ObservadorLance {
    private final String nome;
    private final List<Lance> lancesAuditados = new ArrayList<Lance>();
    AuditorLances(String nome) { this.nome = nome; }

    @Override
    public synchronized void aoReceberLance(LoteLeilao lote, Lance lance, int quantidadeLances) {
        lancesAuditados.add(lance);
        System.out.println("[Auditor " + nome + "] " + lance.dataHora().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                + " | " + lote.nome() + " | " + lance.licitante().nome() + " | R$ "
                + TextoMonetario.formatar(lance.valor()));
    }

    synchronized List<Lance> lancesAuditados() {
        return Collections.unmodifiableList(new ArrayList<Lance>(lancesAuditados));
    }
}

interface RegraComissao {
    BigDecimal calcular(BigDecimal valorVenda);
    String descricao();
}

final class ComissaoArte implements RegraComissao {
    @Override public BigDecimal calcular(BigDecimal preco) { return preco.multiply(new BigDecimal("0.20")); }
    @Override public String descricao() { return "Arte: 20%"; }
}

final class ComissaoVeiculos implements RegraComissao {
    @Override
    public BigDecimal calcular(BigDecimal preco) {
        return preco.multiply(new BigDecimal("0.08")).min(new BigDecimal("5000.00"));
    }
    @Override public String descricao() { return "Veículos: 8%, limitada a R$ 5.000,00"; }
}

final class ComissaoImoveis implements RegraComissao {
    @Override
    public BigDecimal calcular(BigDecimal preco) {
        return preco.multiply(new BigDecimal("0.03")).add(new BigDecimal("1500.00"));
    }
    @Override public String descricao() { return "Imóveis: 3% + R$ 1.500,00"; }
}

final class ComissaoEletronicos implements RegraComissao {
    @Override
    public BigDecimal calcular(BigDecimal preco) {
        return preco.multiply(new BigDecimal("0.12")).max(new BigDecimal("50.00"));
    }
    @Override public String descricao() { return "Eletrônicos: 12%, mínimo de R$ 50,00"; }
}

final class CategoriaArte implements Categoria {
    @Override public String rotulo() { return "Arte"; }
    @Override public RegraComissao regraComissaoPadrao() { return new ComissaoArte(); }
}

final class CategoriaVeiculos implements Categoria {
    @Override public String rotulo() { return "Veículos"; }
    @Override public RegraComissao regraComissaoPadrao() { return new ComissaoVeiculos(); }
}

final class CategoriaImoveis implements Categoria {
    @Override public String rotulo() { return "Imóveis"; }
    @Override public RegraComissao regraComissaoPadrao() { return new ComissaoImoveis(); }
}

final class CategoriaEletronicos implements Categoria {
    @Override public String rotulo() { return "Eletrônicos"; }
    @Override public RegraComissao regraComissaoPadrao() { return new ComissaoEletronicos(); }
}

final class ResultadoLeilao {
    private final boolean vendido;
    private final Lance lanceVencedor;
    private final LoteLeilao lote;
    ResultadoLeilao(boolean vendido, Lance lanceVencedor, LoteLeilao lote) {
        this.vendido = vendido;
        this.lanceVencedor = lanceVencedor;
        this.lote = lote;
    }
    boolean vendido() { return vendido; }
    Lance lanceVencedor() { return lanceVencedor; }
    LoteLeilao lote() { return lote; }
}

final class LoteLeilao {
    private final String nome;
    private final Categoria categoria;
    private final BigDecimal precoInicial;
    private final BigDecimal precoReserva;
    private final List<ObservadorLance> observadores = new ArrayList<ObservadorLance>();
    private final List<Lance> lances = new ArrayList<Lance>();
    private RegraComissao regraComissao;
    private boolean encerrado;

    LoteLeilao(String nome, Categoria categoria, BigDecimal precoInicial, BigDecimal precoReserva) {
        if (nome == null || nome.trim().isEmpty() || categoria == null) {
            throw new IllegalArgumentException("Nome e categoria são obrigatórios.");
        }
        if (precoInicial == null || precoReserva == null || precoInicial.signum() < 0
                || precoReserva.signum() < 0) {
            throw new IllegalArgumentException("Os preços devem ser valores não negativos.");
        }
        this.nome = nome;
        this.categoria = categoria;
        this.precoInicial = precoInicial;
        this.precoReserva = precoReserva;
        this.regraComissao = categoria.regraComissaoPadrao();
    }

    String nome() { return nome; }
    Categoria categoria() { return categoria; }

    synchronized void acompanhar(ObservadorLance observador) {
        if (observador == null) throw new IllegalArgumentException("O observador é obrigatório.");
        if (!observadores.contains(observador)) observadores.add(observador);
    }

    synchronized void deixarAcompanhar(ObservadorLance observador) {
        observadores.remove(observador);
    }

    synchronized void alterarRegraComissao(RegraComissao regra) {
        if (encerrado) throw new IllegalStateException("Não é possível alterar a comissão de um lote encerrado.");
        if (regra == null) throw new IllegalArgumentException("A regra de comissão é obrigatória.");
        regraComissao = regra;
    }

    synchronized boolean ofertarLance(Licitante licitante, BigDecimal valor) {
        if (encerrado) {
            System.out.println("Lance recusado: o lote " + nome + " está encerrado.");
            return false;
        }
        if (licitante == null || valor == null || valor.signum() <= 0) {
            System.out.println("Lance recusado: participante e valor positivo são obrigatórios.");
            return false;
        }
        BigDecimal lanceAtual = lanceAtual();
        BigDecimal lanceMinimo = lanceAtual.multiply(new BigDecimal("1.05"));
        if (valor.compareTo(lanceMinimo) < 0) {
            System.out.println("Lance recusado para " + nome + ": mínimo de R$ "
                    + TextoMonetario.formatar(lanceMinimo) + ".");
            return false;
        }
        Lance lance = new Lance(licitante, valor);
        lances.add(lance);
        for (ObservadorLance observador : new ArrayList<ObservadorLance>(observadores)) {
            if (observador != (Object) licitante) {
                observador.aoReceberLance(this, lance, lances.size());
            }
        }
        return true;
    }

    synchronized ResultadoLeilao encerrar() {
        if (encerrado) throw new IllegalStateException("O lote já foi encerrado.");
        encerrado = true;
        Lance maiorLance = lances.isEmpty() ? null : lances.get(lances.size() - 1);
        boolean vendido = maiorLance != null && maiorLance.valor().compareTo(precoReserva) >= 0;
        if (vendido) {
            System.out.println("\nLote vendido: " + nome + " por R$ " + TextoMonetario.formatar(maiorLance.valor())
                    + " | regra de comissão: " + regraComissao.descricao());
        } else {
            System.out.println("\nLote não vendido: " + nome + " (preço de reserva não atingido).");
        }
        return new ResultadoLeilao(vendido, maiorLance, this);
    }

    synchronized BigDecimal lanceAtual() {
        return lances.isEmpty() ? precoInicial : lances.get(lances.size() - 1).valor();
    }

    synchronized int quantidadeLances() { return lances.size(); }

    synchronized List<Lance> lances() {
        return Collections.unmodifiableList(new ArrayList<Lance>(lances));
    }

    synchronized BigDecimal calcularComissao(BigDecimal valorVenda) {
        if (!encerrado) throw new IllegalStateException("A comissão é calculada no encerramento do lote.");
        return regraComissao.calcular(valorVenda);
    }
}

final class AcertoVendedor {
    private AcertoVendedor() {}

    static void imprimir(ResultadoLeilao resultado) {
        if (!resultado.vendido()) throw new IllegalArgumentException("NÃ£o hÃ¡ acerto para um lote nÃ£o vendido.");
        BigDecimal valorVenda = resultado.lanceVencedor().valor();
        BigDecimal comissao = resultado.lote().calcularComissao(valorVenda);
        System.out.println("\n=== Acerto do vendedor ===");
        System.out.println("Valor de venda: R$ " + TextoMonetario.formatar(valorVenda));
        System.out.println("ComissÃ£o cobrada: R$ " + TextoMonetario.formatar(comissao));
        System.out.println("Valor lÃ­quido: R$ " + TextoMonetario.formatar(valorVenda.subtract(comissao)));
    }
}

final class ItemFatura {
    private final String descricao;
    private final BigDecimal custo;
    ItemFatura(String descricao, BigDecimal custo) {
        this.descricao = descricao;
        this.custo = custo;
    }
    String descricao() { return descricao; }
    BigDecimal custo() { return custo; }
}

interface Compra {
    String nomeLote();
    List<ItemFatura> itensFatura();
    BigDecimal valorVenda();
    BigDecimal valorTotal();
}

final class Venda implements Compra {
    private final String nomeLote;
    private final BigDecimal valorVenda;
    Venda(ResultadoLeilao resultado) {
        if (!resultado.vendido()) throw new IllegalArgumentException("Uma compra exige um lote vendido.");
        this.nomeLote = resultado.lote().nome();
        this.valorVenda = resultado.lanceVencedor().valor();
    }
    @Override public String nomeLote() { return nomeLote; }
    @Override public List<ItemFatura> itensFatura() {
        return Collections.singletonList(new ItemFatura("Valor de venda", valorVenda));
    }
    @Override public BigDecimal valorVenda() { return valorVenda; }
    @Override public BigDecimal valorTotal() { return valorVenda; }
}

abstract class DecoradorCompra implements Compra {
    private final Compra compra;
    DecoradorCompra(Compra compra) {
        if (compra == null) throw new IllegalArgumentException("A compra decorada Ã© obrigatÃ³ria.");
        this.compra = compra;
    }
    protected Compra compra() { return compra; }
    protected abstract ItemFatura itemServico();
    @Override public String nomeLote() { return compra.nomeLote(); }
    @Override public List<ItemFatura> itensFatura() {
        List<ItemFatura> resultado = new ArrayList<ItemFatura>(compra.itensFatura());
        resultado.add(itemServico());
        return Collections.unmodifiableList(resultado);
    }
    @Override public BigDecimal valorVenda() { return compra.valorVenda(); }
    @Override public BigDecimal valorTotal() { return compra.valorTotal().add(itemServico().custo()); }
}

final class SeguroTransporte extends DecoradorCompra {
    SeguroTransporte(Compra compra) { super(compra); }
    @Override protected ItemFatura itemServico() {
        return new ItemFatura("Seguro de transporte (1,5%)",
                compra().valorVenda().multiply(new BigDecimal("0.015")));
    }
}

final class CertificadoAutenticidade extends DecoradorCompra {
    CertificadoAutenticidade(Compra compra) { super(compra); }
    @Override protected ItemFatura itemServico() {
        return new ItemFatura("Certificado de autenticidade", new BigDecimal("300.00"));
    }
}

final class EmbalagemEspecial extends DecoradorCompra {
    EmbalagemEspecial(Compra compra) { super(compra); }
    @Override protected ItemFatura itemServico() {
        return new ItemFatura("Embalagem especial", new BigDecimal("180.00"));
    }
}

final class EntregaExpressa extends DecoradorCompra {
    EntregaExpressa(Compra compra) { super(compra); }
    @Override protected ItemFatura itemServico() {
        return new ItemFatura("Entrega expressa", new BigDecimal("450.00"));
    }
}

final class FaturaComprador {
    private FaturaComprador() {}
    static void imprimir(Compra compra) {
        System.out.println("\n=== Fatura do comprador ===");
        System.out.println("Lote: " + compra.nomeLote());
        for (ItemFatura itemFatura : compra.itensFatura()) {
            System.out.println(itemFatura.descricao() + ": R$ " + TextoMonetario.formatar(itemFatura.custo()));
        }
        System.out.println("Total a pagar: R$ " + TextoMonetario.formatar(compra.valorTotal()));
    }
}

final class CasaLeiloes {
    void aplicarCampanhaComissao(LoteLeilao lote, RegraComissao regra) {
        lote.alterarRegraComissao(regra);
    }
}

final class TextoMonetario {
    private TextoMonetario() {}
    static String formatar(BigDecimal valor) {
        return valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}