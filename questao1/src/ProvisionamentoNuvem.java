package questao1;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

interface RecursoNuvem {
    String provedor();
    String identificador();
    BigDecimal custoMensal();
}

interface MaquinaVirtual extends RecursoNuvem {}

interface ArmazenamentoObjetos extends RecursoNuvem {}

interface FilaMensagens extends RecursoNuvem {}

interface FabricaRecursosNuvem {
    String nomeProvedor();
    BigDecimal estimarCustoMensal(int quantidadeMaquinas, int armazenamentoGb);
    MaquinaVirtual criarMaquinaVirtual();
    ArmazenamentoObjetos criarArmazenamentoObjetos(int armazenamentoGb);
    FilaMensagens criarFilaMensagens();
}

final class FabricaRecursosAws implements FabricaRecursosNuvem {
    private static final BigDecimal CUSTO_HORA_MAQUINA = new BigDecimal("0.10");
    private static final BigDecimal CUSTO_MENSAL_GB = new BigDecimal("0.023");
    private static final BigDecimal CUSTO_MENSAL_FILA = new BigDecimal("0.40");
    private final AtomicInteger sequenciaMaquinas = new AtomicInteger();
    private final AtomicInteger sequenciaArmazenamento = new AtomicInteger();
    private final AtomicInteger sequenciaFilas = new AtomicInteger();

    @Override public String nomeProvedor() { return "AWS"; }

    @Override
    public BigDecimal estimarCustoMensal(int quantidadeMaquinas, int armazenamentoGb) {
        return CUSTO_HORA_MAQUINA.multiply(BigDecimal.valueOf(quantidadeMaquinas))
                .multiply(BigDecimal.valueOf(730))
                .add(CUSTO_MENSAL_GB.multiply(BigDecimal.valueOf(armazenamentoGb)))
                .add(CUSTO_MENSAL_FILA);
    }

    @Override
    public MaquinaVirtual criarMaquinaVirtual() {
        return new MaquinaVirtualAws(sequenciaMaquinas.incrementAndGet());
    }

    @Override
    public ArmazenamentoObjetos criarArmazenamentoObjetos(int armazenamentoGb) {
        return new ArmazenamentoObjetosAws(sequenciaArmazenamento.incrementAndGet(), armazenamentoGb);
    }

    @Override
    public FilaMensagens criarFilaMensagens() {
        return new FilaMensagensAws(sequenciaFilas.incrementAndGet());
    }

    private static final class MaquinaVirtualAws implements MaquinaVirtual {
        private final int sequencia;
        private MaquinaVirtualAws(int sequencia) { this.sequencia = sequencia; }
        @Override public String provedor() { return "AWS"; }
        @Override public String identificador() { return "i-" + sequencia; }
        @Override public BigDecimal custoMensal() { return CUSTO_HORA_MAQUINA.multiply(BigDecimal.valueOf(730)); }
    }

    private static final class ArmazenamentoObjetosAws implements ArmazenamentoObjetos {
        private final int sequencia;
        private final int armazenamentoGb;
        private ArmazenamentoObjetosAws(int sequencia, int armazenamentoGb) {
            this.sequencia = sequencia;
            this.armazenamentoGb = armazenamentoGb;
        }
        @Override public String provedor() { return "AWS"; }
        @Override public String identificador() { return "s3://" + sequencia; }
        @Override public BigDecimal custoMensal() { return CUSTO_MENSAL_GB.multiply(BigDecimal.valueOf(armazenamentoGb)); }
    }

    private static final class FilaMensagensAws implements FilaMensagens {
        private final int sequencia;
        private FilaMensagensAws(int sequencia) { this.sequencia = sequencia; }
        @Override public String provedor() { return "AWS"; }
        @Override public String identificador() { return "sqs-" + sequencia; }
        @Override public BigDecimal custoMensal() { return CUSTO_MENSAL_FILA; }
    }
}

final class FabricaRecursosAzure implements FabricaRecursosNuvem {
    private static final BigDecimal CUSTO_HORA_MAQUINA = new BigDecimal("0.12");
    private static final BigDecimal CUSTO_MENSAL_GB = new BigDecimal("0.02");
    private static final BigDecimal CUSTO_MENSAL_FILA = new BigDecimal("0.50");
    private final AtomicInteger sequenciaMaquinas = new AtomicInteger();
    private final AtomicInteger sequenciaArmazenamento = new AtomicInteger();
    private final AtomicInteger sequenciaFilas = new AtomicInteger();

    @Override public String nomeProvedor() { return "AZURE"; }

    @Override
    public BigDecimal estimarCustoMensal(int quantidadeMaquinas, int armazenamentoGb) {
        return CUSTO_HORA_MAQUINA.multiply(BigDecimal.valueOf(quantidadeMaquinas))
                .multiply(BigDecimal.valueOf(730))
                .add(CUSTO_MENSAL_GB.multiply(BigDecimal.valueOf(armazenamentoGb)))
                .add(CUSTO_MENSAL_FILA);
    }

    @Override
    public MaquinaVirtual criarMaquinaVirtual() {
        return new MaquinaVirtualAzure(sequenciaMaquinas.incrementAndGet());
    }

    @Override
    public ArmazenamentoObjetos criarArmazenamentoObjetos(int armazenamentoGb) {
        return new ArmazenamentoObjetosAzure(sequenciaArmazenamento.incrementAndGet(), armazenamentoGb);
    }

    @Override
    public FilaMensagens criarFilaMensagens() {
        return new FilaMensagensAzure(sequenciaFilas.incrementAndGet());
    }

    private static final class MaquinaVirtualAzure implements MaquinaVirtual {
        private final int sequencia;
        private MaquinaVirtualAzure(int sequencia) { this.sequencia = sequencia; }
        @Override public String provedor() { return "AZURE"; }
        @Override public String identificador() { return "vm-" + sequencia; }
        @Override public BigDecimal custoMensal() { return CUSTO_HORA_MAQUINA.multiply(BigDecimal.valueOf(730)); }
    }

    private static final class ArmazenamentoObjetosAzure implements ArmazenamentoObjetos {
        private final int sequencia;
        private final int armazenamentoGb;
        private ArmazenamentoObjetosAzure(int sequencia, int armazenamentoGb) {
            this.sequencia = sequencia;
            this.armazenamentoGb = armazenamentoGb;
        }
        @Override public String provedor() { return "AZURE"; }
        @Override public String identificador() { return "blob://" + sequencia; }
        @Override public BigDecimal custoMensal() { return CUSTO_MENSAL_GB.multiply(BigDecimal.valueOf(armazenamentoGb)); }
    }

    private static final class FilaMensagensAzure implements FilaMensagens {
        private final int sequencia;
        private FilaMensagensAzure(int sequencia) { this.sequencia = sequencia; }
        @Override public String provedor() { return "AZURE"; }
        @Override public String identificador() { return "sb-" + sequencia; }
        @Override public BigDecimal custoMensal() { return CUSTO_MENSAL_FILA; }
    }
}

final class SolicitacaoProvisionamento {
    private final String nomeAmbiente;
    private final String provedor;
    private final int quantidadeMaquinas;
    private final int armazenamentoGb;

    SolicitacaoProvisionamento(String nomeAmbiente, String provedor, int quantidadeMaquinas, int armazenamentoGb) {
        if (nomeAmbiente == null || nomeAmbiente.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do ambiente é obrigatório.");
        }
        if (provedor == null || provedor.trim().isEmpty()) {
            throw new IllegalArgumentException("O provedor é obrigatório.");
        }
        if (quantidadeMaquinas < 0 || armazenamentoGb < 0) {
            throw new IllegalArgumentException("As quantidades não podem ser negativas.");
        }
        this.nomeAmbiente = nomeAmbiente;
        this.provedor = provedor.trim().toUpperCase(Locale.ROOT);
        this.quantidadeMaquinas = quantidadeMaquinas;
        this.armazenamentoGb = armazenamentoGb;
    }

    String nomeAmbiente() { return nomeAmbiente; }
    String provedor() { return provedor; }
    int quantidadeMaquinas() { return quantidadeMaquinas; }
    int armazenamentoGb() { return armazenamentoGb; }
}

final class AmbienteNuvem {
    private final String nome;
    private final String provedor;
    private final List<RecursoNuvem> recursos;
    private final BigDecimal custoMensal;

    AmbienteNuvem(String nome, String provedor, List<RecursoNuvem> recursos) {
        for (RecursoNuvem recurso : recursos) {
            if (!provedor.equals(recurso.provedor())) {
                throw new IllegalArgumentException("Recursos de provedores diferentes não podem compor o mesmo ambiente.");
            }
        }
        this.nome = nome;
        this.provedor = provedor;
        this.recursos = Collections.unmodifiableList(new ArrayList<RecursoNuvem>(recursos));
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (RecursoNuvem recurso : recursos) {
            valorTotal = valorTotal.add(recurso.custoMensal());
        }
        this.custoMensal = valorTotal;
    }

    BigDecimal custoMensal() { return custoMensal; }

    void imprimirResumo() {
        StringBuilder resumo = new StringBuilder();
        resumo.append("\nProvisionamento aceito: ").append(nome).append(System.lineSeparator());
        resumo.append("Provedor: ").append(provedor)
                .append(" | Região: ").append(ConfiguracaoAplicacao.obterInstancia().regiao())
                .append(System.lineSeparator());
        for (RecursoNuvem recurso : recursos) {
            resumo.append("  ").append(recurso.getClass().getSimpleName()).append(" ")
                    .append(recurso.identificador()).append(" | custo mensal: R$ ")
                    .append(formatarValor(recurso.custoMensal())).append(System.lineSeparator());
        }
        resumo.append("Custo mensal estimado: R$ ").append(formatarValor(custoMensal));
        System.out.println(resumo);
    }

    private static String formatarValor(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

final class ConfiguracaoAplicacao {
    private static final ConfiguracaoAplicacao INSTANCIA = carregarUmaVez();
    private final String regiao;
    private final BigDecimal orcamentoMensal;

    private ConfiguracaoAplicacao(String regiao, BigDecimal orcamentoMensal) {
        this.regiao = regiao;
        this.orcamentoMensal = orcamentoMensal;
    }

    static ConfiguracaoAplicacao obterInstancia() { return INSTANCIA; }
    String regiao() { return regiao; }
    BigDecimal orcamentoMensal() { return orcamentoMensal; }

    private static ConfiguracaoAplicacao carregarUmaVez() {
        Properties propriedades = new Properties();
        Path caminhoConfiguracao = Paths.get("config.properties");
        try (InputStream entrada = Files.newInputStream(caminhoConfiguracao)) {
            propriedades.load(entrada);
        } catch (IOException excecao) {
            throw new ExceptionInInitializerError("Não foi possível ler " + caminhoConfiguracao.toAbsolutePath()
                    + ": " + excecao.getMessage());
        }
        String regiao = propriedades.getProperty("regiao");
        String orcamento = propriedades.getProperty("orcamentoMensal");
        if (regiao == null || regiao.trim().isEmpty() || orcamento == null) {
            throw new ExceptionInInitializerError("config.properties deve definir regiao e orcamentoMensal.");
        }
        try {
            return new ConfiguracaoAplicacao(regiao.trim(), new BigDecimal(orcamento.trim()));
        } catch (NumberFormatException excecao) {
            throw new ExceptionInInitializerError("orcamentoMensal deve ser um valor numérico válido.");
        }
    }
}

final class RegistroAuditoria {
    private final LocalDateTime dataHora;
    private final String nomeAmbiente;
    private final String provedor;
    private final String resultado;

    RegistroAuditoria(String nomeAmbiente, String provedor, String resultado) {
        this.dataHora = LocalDateTime.now();
        this.nomeAmbiente = nomeAmbiente;
        this.provedor = provedor;
        this.resultado = resultado;
    }

    @Override
    public String toString() {
        return dataHora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " | " + nomeAmbiente
                + " | " + provedor + " | " + resultado;
    }
}

final class HistoricoAuditoria {
    private static final HistoricoAuditoria INSTANCIA = new HistoricoAuditoria();
    private final List<RegistroAuditoria> registros = new ArrayList<RegistroAuditoria>();

    private HistoricoAuditoria() {}
    static HistoricoAuditoria obterInstancia() { return INSTANCIA; }

    synchronized void registrar(String nomeAmbiente, String provedor, String resultado) {
        registros.add(new RegistroAuditoria(nomeAmbiente, provedor, resultado));
    }

    synchronized void imprimirHistorico() {
        System.out.println("\n=== Histórico de auditoria ===");
        for (RegistroAuditoria registro : registros) {
            System.out.println(registro);
        }
    }
}

final class ProvisionadorAmbiente {
    AmbienteNuvem provisionar(SolicitacaoProvisionamento solicitacao, FabricaRecursosNuvem fabrica) {
        if (!solicitacao.provedor().equals(fabrica.nomeProvedor())) {
            HistoricoAuditoria.obterInstancia().registrar(solicitacao.nomeAmbiente(), solicitacao.provedor(),
                    "RECUSADO: provedor da solicitação não corresponde à fábrica");
            System.out.println("Provisionamento recusado: " + solicitacao.nomeAmbiente()
                    + " - provedor incompatível.");
            return null;
        }

        BigDecimal estimativa = fabrica.estimarCustoMensal(solicitacao.quantidadeMaquinas(), solicitacao.armazenamentoGb());
        ConfiguracaoAplicacao configuracao = ConfiguracaoAplicacao.obterInstancia();
        if (estimativa.compareTo(configuracao.orcamentoMensal()) > 0) {
            HistoricoAuditoria.obterInstancia().registrar(solicitacao.nomeAmbiente(), solicitacao.provedor(),
                    "RECUSADO: orçamento excedido (R$ " + formatarValor(estimativa) + " > R$ "
                            + formatarValor(configuracao.orcamentoMensal()) + ")");
            System.out.println("Provisionamento recusado: " + solicitacao.nomeAmbiente()
                    + " - orçamento mensal excedido.");
            return null;
        }

        List<RecursoNuvem> recursos = new ArrayList<RecursoNuvem>();
        for (int i = 0; i < solicitacao.quantidadeMaquinas(); i++) {
            recursos.add(fabrica.criarMaquinaVirtual());
        }
        recursos.add(fabrica.criarArmazenamentoObjetos(solicitacao.armazenamentoGb()));
        recursos.add(fabrica.criarFilaMensagens());
        AmbienteNuvem ambiente = new AmbienteNuvem(solicitacao.nomeAmbiente(), fabrica.nomeProvedor(),
                recursos);
        if (ambiente.custoMensal().compareTo(estimativa) != 0) {
            throw new IllegalStateException("O custo dos recursos não corresponde à estimativa da fábrica.");
        }
        HistoricoAuditoria.obterInstancia().registrar(solicitacao.nomeAmbiente(), solicitacao.provedor(), "ACEITO");
        ambiente.imprimirResumo();
        return ambiente;
    }

    private static String formatarValor(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
