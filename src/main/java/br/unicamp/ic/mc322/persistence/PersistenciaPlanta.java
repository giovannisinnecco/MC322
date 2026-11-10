package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.*;
import br.unicamp.ic.mc322.model.*;
import br.unicamp.ic.mc322.service.*;
import br.unicamp.ic.mc322.strategy.*;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** Salva e reconstrói o estado recuperável da fábrica em XML. */
public final class PersistenciaPlanta {
    private final Path arquivo;

    /**
     * Cria o serviço associado ao arquivo informado.
     * @param arquivo caminho XML que será lido e escrito
     * @throws IllegalArgumentException se o caminho for nulo
     */
    public PersistenciaPlanta(Path arquivo) {
        if (arquivo == null) throw new IllegalArgumentException("Informe o caminho do arquivo XML.");
        this.arquivo = arquivo.toAbsolutePath();
    }

    /**
     * Escreve um retrato completo dos dados operacionais obrigatórios.
     * @param fabrica fábrica a persistir
     * @throws IllegalArgumentException se a fábrica for nula
     * @throws PersistenciaException se a escrita falhar ou a fábrica não tiver cenário suportado
     */
    public void salvar(GerenciadorProducao fabrica) {
        if (fabrica == null) throw new IllegalArgumentException("Informe a fábrica.");
        if (fabrica.getCenario() == null) {
            throw new PersistenciaException("A persistência exige uma fábrica associada a um cenário conhecido.");
        }
        Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
        try {
            Document doc = novoDocumento();
            Element root = doc.createElement("planta");
            root.setAttribute("versao", "1");
            root.setAttribute("cenario", fabrica.getCenario().name());
            root.setAttribute("orcamento", Double.toString(fabrica.getBudget()));
            root.setAttribute("estrategia", serializarEstrategia(fabrica.getNomeEstrategia()));
            doc.appendChild(root);

            MateriaPrima mp = fabrica.getMateriaPrima();
            Element estoque = filho(doc, root, "materiasPrimas");
            Element item = filho(doc, estoque, "materiaPrima");
            item.setAttribute("id", Integer.toString(mp.getId()));
            item.setAttribute("nome", mp.getNome());
            item.setAttribute("unidade", mp.getUnidade());
            item.setAttribute("minimo", Double.toString(mp.getQuantidadeMinima()));
            item.setAttribute("custoPorUnidade", Double.toString(mp.getCustoPorUnidade()));
            item.setAttribute("quantidade", Double.toString(mp.getQuantidade()));

            Element linha = filho(doc, root, "maquinas");
            for (Maquina maquina : fabrica.getMaquinas()) {
                Element m = filho(doc, linha, "maquina");
                m.setAttribute("tipo", maquina.getClass().getSimpleName());
                m.setAttribute("nome", maquina.getNome());
                m.setAttribute("estado", maquina.getEstado().name());
                m.setAttribute("saude", Double.toString(maquina.getSaude()));
                m.setAttribute("capacidade", Integer.toString(maquina.getCapacidadeMaxima()));
                m.setAttribute("limiteManutencao", Double.toString(maquina.getLimiteManutencao()));
                m.setAttribute("probabilidadeFalhaBase", Double.toString(maquina.getProbabilidadeFalhaBase()));
                m.setAttribute("custoOperacao", Double.toString(maquina.getCustoOperacao()));
                Element componentes = filho(doc, m, "componentes");
                Element controlador = filho(doc, componentes, "controladorEtapa");
                controlador.setAttribute("estado", maquina.estaLigada() ? "ATIVO" : "EM_ESPERA");
                Element sensor = filho(doc, componentes, "sensorAlinhamento");
                sensor.setAttribute("limiarSaude", "10.0");
                Element atuador = filho(doc, componentes, "atuadorPrecisao");
                atuador.setAttribute("resolucao", Integer.toString(maquina.getCapacidadeMaxima()));
            }

            Element pedidos = filho(doc, root, "demandas");
            for (Demanda demanda : fabrica.getDemandas()) {
                Element d = filho(doc, pedidos, "demanda");
                d.setAttribute("tipoProduto", demanda.getTipoProduto());
                d.setAttribute("quantidade", Integer.toString(demanda.getQuantidadeProdutos()));
                d.setAttribute("consumo", Integer.toString(consumo(demanda.getTipoProduto())));
                d.setAttribute("custo", Double.toString(demanda.getCustoPorUnidade()));
                d.setAttribute("estado", demanda.getStatus().name());
            }
            Files.createDirectories(arquivo.getParent());
            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(temporario.toFile()));
            try {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            try { Files.deleteIfExists(temporario); } catch (IOException ignored) { /* preserva a falha original */ }
            throw new PersistenciaException("Não foi possível salvar a planta em " + arquivo + ".", e);
        }
    }

    /**
     * Lê e valida todo o XML antes de devolver uma nova fábrica.
     * @return nova fábrica reconstruída sem alterar outras instâncias
     * @throws PersistenciaException se o arquivo estiver ausente, inválido ou incompatível
     */
    public GerenciadorProducao carregar() {
        if (!Files.isRegularFile(arquivo)) throw new PersistenciaException("Arquivo de persistência inexistente: " + arquivo);
        try {
            var builder = DocumentBuilderFactory.newInstance();
            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            builder.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            builder.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var leitor = builder.newDocumentBuilder();
            leitor.setErrorHandler(new DefaultHandler() {
                @Override public void warning(SAXParseException e) throws SAXParseException { throw e; }
                @Override public void error(SAXParseException e) throws SAXParseException { throw e; }
                @Override public void fatalError(SAXParseException e) throws SAXParseException { throw e; }
            });
            Document doc = leitor.parse(arquivo.toFile());
            Element root = doc.getDocumentElement();
            exigir("planta".equals(root.getTagName()) && "1".equals(root.getAttribute("versao")), "Raiz ou versão XML inválida.");
            Cenario cenario = Cenario.valueOf(atributo(root, "cenario"));
            double budget = decimal(root, "orcamento");
            MateriaPrima mp = lerEstoque(unico(root, "materiasPrimas"));

            NodeList lista = unico(root, "maquinas").getElementsByTagName("maquina");
            exigir(lista.getLength() == 3, "A linha deve conter exatamente três máquinas.");
            Maquina[] maquinas = new Maquina[3];
            String[] esperados = {"MaquinaFotolitografia", "MaquinaEncapsulamento", "EstacaoInspecao"};
            for (int i = 0; i < 3; i++) {
                Element e = (Element) lista.item(i);
                exigir(esperados[i].equals(atributo(e, "tipo")), "Tipo ou ordem de máquina incompatível.");
                int capacidade = inteiro(e, "capacidade");
                maquinas[i] = switch (i) {
                    case 0 -> new MaquinaFotolitografia(capacidade);
                    case 1 -> new MaquinaEncapsulamento(capacidade);
                    default -> new EstacaoInspecao(capacidade);
                };
                exigir(atributo(e, "nome").equals(maquinas[i].getNome()), "Nome da máquina incompatível.");
                exigir(decimal(e, "probabilidadeFalhaBase") == maquinas[i].getProbabilidadeFalhaBase(),
                        "Configuração de falha da máquina incompatível.");
                exigir(decimal(e, "custoOperacao") == maquinas[i].getCustoOperacao(),
                        "Custo operacional da máquina incompatível.");
                double saude = decimal(e, "saude");
                maquinas[i].setSaude(saude);
                maquinas[i].setLimiteManutencao(decimal(e, "limiteManutencao"));
                EstadoMaquina estadoSalvo = EstadoMaquina.valueOf(atributo(e, "estado"));
                exigir((saude == 0) == (estadoSalvo == EstadoMaquina.QUEBRADA), "Saúde e estado da máquina incompatíveis.");
                validarComponentes(unico(e, "componentes"), capacidade);
                String estadoControlador = atributo(unico(unico(e, "componentes"), "controladorEtapa"), "estado");
                exigir((estadoSalvo == EstadoMaquina.LIGADA) == "ATIVO".equals(estadoControlador),
                        "Estado do controlador incompatível com a máquina.");
                if (estadoSalvo == EstadoMaquina.LIGADA) maquinas[i].ligar();
            }
            GerenciadorProducao fabrica = new GerenciadorProducao(mp, budget,
                    (MaquinaFotolitografia) maquinas[0], (MaquinaEncapsulamento) maquinas[1], (EstacaoInspecao) maquinas[2]);
            fabrica.restaurarCenario(cenario);
            fabrica.setEstrategia(parseEstrategia(atributo(root, "estrategia")));
            NodeList demandas = unico(root, "demandas").getElementsByTagName("demanda");
            for (int i = 0; i < demandas.getLength(); i++) {
                Element e = (Element) demandas.item(i);
                String tipo = atributo(e, "tipoProduto");
                int quantidade = inteiro(e, "quantidade");
                int consumo = inteiro(e, "consumo");
                exigir(consumo == consumo(tipo), "Consumo da demanda incompatível com o modelo.");
                double custo = decimal(e, "custo");
                Demanda d = new Demanda(tipo, quantidade, consumo, custo);
                d.restaurarEstado(quantidade, StatusDemanda.valueOf(atributo(e, "estado")));
                fabrica.restaurarDemanda(d);
            }
            return fabrica;
        } catch (PersistenciaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("Arquivo XML inválido, corrompido ou incompatível com a fábrica.", e);
        }
    }

    private static MateriaPrima lerEstoque(Element wrapper) {
        NodeList nodes = wrapper.getElementsByTagName("materiaPrima");
        exigir(nodes.getLength() == 1, "O XML deve conter um estoque de matéria-prima.");
        Element e = (Element) nodes.item(0);
        return new MateriaPrima(inteiro(e, "id"), atributo(e, "nome"), decimal(e, "quantidade"),
                atributo(e, "unidade"), decimal(e, "minimo"), decimal(e, "custoPorUnidade"));
    }
    private static void validarComponentes(Element e, int capacidade) {
        Element c = unico(e, "controladorEtapa");
        exigir("ATIVO".equals(atributo(c, "estado")) || "EM_ESPERA".equals(atributo(c, "estado")), "Estado do controlador inválido.");
        exigir(decimal(unico(e, "sensorAlinhamento"), "limiarSaude") == 10.0, "Configuração do sensor incompatível.");
        exigir(inteiro(unico(e, "atuadorPrecisao"), "resolucao") == capacidade, "Configuração do atuador incompatível.");
        exigir(e.getElementsByTagName("controladorEtapa").getLength() == 1
                && e.getElementsByTagName("sensorAlinhamento").getLength() == 1
                && e.getElementsByTagName("atuadorPrecisao").getLength() == 1, "Composição de máquina inválida.");
    }
    private static EstrategiaProducao parseEstrategia(String id) {
        return switch (id) {
            case "fila" -> new EstrategiaFilaSilicio();
            case "maiorLote" -> new EstrategiaMaiorLoteSTM32();
            case "rendimento" -> new EstrategiaRendimentoChips();
            default -> throw new PersistenciaException("Estratégia persistida desconhecida: " + id);
        };
    }
    private static String serializarEstrategia(String nome) {
        if (nome.startsWith("Fila de silício")) return "fila";
        if (nome.startsWith("Maior lote")) return "maiorLote";
        if (nome.startsWith("Rendimento")) return "rendimento";
        throw new PersistenciaException("Estratégia não suportada pela persistência.");
    }
    private static int consumo(String tipo) {
        return switch (tipo) {
            case "STM32G" -> STM32G.CONSUMO_MATERIA_PRIMA;
            case "STM32F" -> STM32F.CONSUMO_MATERIA_PRIMA;
            case "STM32H" -> STM32H.CONSUMO_MATERIA_PRIMA;
            default -> throw new PersistenciaException("Modelo de demanda desconhecido: " + tipo);
        };
    }
    private static Document novoDocumento() throws Exception { return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); }
    private static Element filho(Document doc, Element pai, String nome) { Element e = doc.createElement(nome); pai.appendChild(e); return e; }
    private static Element unico(Element pai, String nome) {
        NodeList nodes = pai.getElementsByTagName(nome);
        if (nodes.getLength() != 1) throw new PersistenciaException("Elemento " + nome + " ausente ou repetido.");
        Node n = nodes.item(0);
        if (!(n instanceof Element e)) throw new PersistenciaException("Elemento XML inválido: " + nome);
        return e;
    }
    private static String atributo(Element e, String nome) {
        if (!e.hasAttribute(nome) || e.getAttribute(nome).isBlank()) throw new PersistenciaException("Atributo obrigatório ausente: " + nome);
        return e.getAttribute(nome);
    }
    private static int inteiro(Element e, String nome) { return Integer.parseInt(atributo(e, nome)); }
    private static double decimal(Element e, String nome) {
        double v = Double.parseDouble(atributo(e, nome));
        exigir(Double.isFinite(v), "Número não finito: " + nome);
        return v;
    }
    private static void exigir(boolean condicao, String mensagem) { if (!condicao) throw new PersistenciaException(mensagem); }
}
