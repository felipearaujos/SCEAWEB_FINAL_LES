/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package scea.web.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.servlet.http.HttpSession;
import org.primefaces.event.SelectEvent;
import scea.core.aplicacao.Estoque;
import scea.core.aplicacao.Resultado;
import scea.core.factories.dominio.ProdutoFactory;
import scea.core.factories.dominio.SimulacaoFactory;
import scea.core.factories.dominio.TransacaoFactory;
import scea.dominio.modelo.Acesso;
import scea.dominio.modelo.EntidadeDominio;
import scea.dominio.modelo.Entrada;
import scea.dominio.modelo.Fornecedor;
import scea.dominio.modelo.Produto;
import scea.dominio.modelo.Saida;
import scea.dominio.modelo.Simulacao;
import scea.dominio.modelo.Transacao;

/**
 *
 * @author Main User
 */
@ApplicationScoped
@ManagedBean (name = "simulacaoBean")
public class SimulacaoBean extends EntidadeDominioBean{
    private String transacao;
    //private Date dt_futura;
    private int quantidade;
    private Produto produto;
    private String id_usuario;
    private String nomeProduto;
    private int id_produto;
    private String login_usuario;
    private List<Simulacao> simulacoes;
    private Simulacao simulacaoSelecionada;
    private Estoque estoque;

    
    public void pegarIdeLoginUsuarioAtual()
    {
        HttpSession session = ( HttpSession ) FacesContext.getCurrentInstance().getExternalContext().getSession(false); 
        setId_usuario( session.getAttribute("id_user").toString());
        setLogin_usuario(session.getAttribute("login_user").toString());
    }

    public void simularEstoque(){
        entidadeFactory = new TransacaoFactory();
        Transacao t = (Transacao)entidadeFactory.createEntidade();
        entidadeFactory = new ProdutoFactory();
        Produto produto = (Produto)entidadeFactory.createEntidade();
        produto.setId(getId_produto());
        produto.setQuantidade(getQuantidade());
        produto.setFornecedor(new Fornecedor());
        produto.getFornecedor().setId(0);
        produto.setNome(null);
        t.setProduto(produto);
        r = fachada.simular(t);
         StringBuilder sb = new StringBuilder();
        if(r.getEntidades() != null){
           
            estoque = (Estoque)r.getEntidades().get(0);
            sb.append("");
            
            if(estoque.isFlgValida()){
                sb.append("SIMULAÇÃO VALIDA  //  \n");
                sb.append("QUANTIDADE FUTURA: ").append(estoque.getQtdeFutura()).append("  //  \n");
            }
            else{
                sb.append("SIMULAÇÃO INVÁLIDA  //  \n");
                sb.append("QUANTIDADE PERMITIDA: ").append(estoque.getQtdeDisponivel()).append("  //  \n");
            }
            
            if(estoque.getObs() != null){
                sb.append("OBSERVAÇÃO: ");
                sb.append(estoque.getObs());
            }
            //sb.append("QUANTIDADE TENTATIVA: ").append(estoque.getQtdeTentativa()).append("   \n");
        }
        
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage mensagem = new FacesMessage(
        FacesMessage.SEVERITY_INFO, "", sb.toString());
        context.addMessage(null, mensagem);
        
        
        
        //estoque.setQtdeDisponivel(0);
        //estoque.setQtdeTentativa(0);
        //estoque.setQtdeFutura(0);
    }
    
    public void pegar(SelectEvent event)
    {
       
    }
    
    public Simulacao createSimulacaoModel()
    {
        entidadeFactory = new SimulacaoFactory();
        Simulacao s = (Simulacao)entidadeFactory.createEntidade();
        s.setQtdeItens(getQuantidade());
        s.setTipoDeTransacao(getTransacao());
        Produto p = new Produto();
        p.setId(getId_produto());
        s.setProduto(p);
        return s;
    }
    
    
    public void Salvar(){

        Simulacao simulacao = this.createSimulacaoModel();
        Resultado r = fachada.salvar(simulacao);
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage mensagem = new FacesMessage(
            FacesMessage.SEVERITY_INFO, "", r.getMsg());
            context.addMessage(null, mensagem);

    }
    
    public void salvarTransacao()
    {
        
        Transacao transacao= new Transacao();
        
        if(simulacaoSelecionada.getTipoDeTransacao().equals("ENTRADA")){
            transacao = new Entrada();
            
            
           
        }
        else if(simulacaoSelecionada.getTipoDeTransacao().equals("SAIDA")){
            transacao = new Saida();
        }
        
       transacao.setQtdeDoTipo(simulacaoSelecionada.getQtdeItens());
        transacao.setTipoDeTransacao(simulacaoSelecionada.getTipoDeTransacao());
        Produto p = new Produto();
        p.setId(getId_produto());
        transacao.setProduto(p);
        transacao.getProduto().setFornecedor(new Fornecedor());
        transacao.getProduto().getFornecedor().setId(0);
        transacao.getProduto().setNome(null);
          
        Acesso acesso = new Acesso();
        acesso.setId(Integer.parseInt(getId_usuario()));
        transacao.setAcesso(acesso);
            
        
         r = fachada.salvar(transacao);
       

        FacesContext context = FacesContext.getCurrentInstance();
        if(r.getMsg() == null){
            FacesMessage mensagem = new FacesMessage(
            FacesMessage.SEVERITY_INFO, "", ("TRANSACAO DE '" + simulacaoSelecionada.getTipoDeTransacao() + "' REALIZADA COM SUCESSO!"));
            context.addMessage(null, mensagem);
        }else{
            FacesMessage mensagem = new FacesMessage(
            FacesMessage.SEVERITY_INFO, "", r.getMsg() );
            context.addMessage(null, mensagem);
    
        }
    }
    
    public List<Simulacao> consultar()
    {
        Resultado r = new Resultado();
        List<EntidadeDominio> entidades = new ArrayList<EntidadeDominio>();
        r = fachada.consultar(createSimulacaoModel());
        entidades = r.getEntidades();
        List<Simulacao> simulacoesBuscadas = new ArrayList<Simulacao>();
        for(EntidadeDominio e: entidades)
        {
            Simulacao s2 = (Simulacao)e;
            simulacoesBuscadas.add(s2);
        }
        setSimulacoes(simulacoesBuscadas);
        return getSimulacoes();
    }
    
    /**
     * @return the transacao
     */
    public String getTransacao() {
        return transacao;
    }

    /**
     * @param transacao the transacao to set
     */
    public void setTransacao(String transacao) {
        this.transacao = transacao;
    }

    /**
     
    public Date getDt_futura() {
        return dt_futura;
    }

    
         public void setDt_futura(Date dt_futura) {
        this.dt_futura = dt_futura;
    }
    */


    /**
     * @return the quantidade
     */
    public int getQuantidade() {
        return quantidade;
    }

    /**
     * @param quantidade the quantidade to set
     */
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    /**
     * @return the produto
     */
    public Produto getProduto() {
        return produto;
    }

    /**
     * @param produto the produto to set
     */
    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    /**
     * @return the id_usuario
     */
    public String getId_usuario() {
        return id_usuario;
    }

    /**
     * @param id_usuario the id_usuario to set
     */
    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    /**
     * @return the login_usuario
     */
    public String getLogin_usuario() {
        return login_usuario;
    }

    /**
     * @param login_usuario the login_usuario to set
     */
    public void setLogin_usuario(String login_usuario) {
        this.login_usuario = login_usuario;
    }

    /**
     * @return the simulacoes
     */
    public List<Simulacao> getSimulacoes() {
        return simulacoes;
    }

    /**
     * @param simulacoes the simulacoes to set
     */
    public void setSimulacoes(List<Simulacao> simulacoes) {
        this.simulacoes = simulacoes;
    }

    /**
     * @return the id_produto
     */
    public int getId_produto() {
        return id_produto;
    }

    /**
     * @param id_produto the id_produto to set
     */
    public void setId_produto(int id_produto) {
        this.id_produto = id_produto;
    }

    /**
     * @return the simulacaoSelecionada
     */
    public Simulacao getSimulacaoSelecionada() {
        return simulacaoSelecionada;
    }

    /**
     * @param simulacaoSelecionada the simulacaoSelecionada to set
     */
    public void setSimulacaoSelecionada(Simulacao simulacaoSelecionada) {
        this.simulacaoSelecionada = simulacaoSelecionada;
    }

    /**
     * @return the nomeProduto
     */
    public String getNomeProduto() {
        return nomeProduto;
    }

    /**
     * @param nomeProduto the nomeProduto to set
     */
    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    /**
     * @return the estoque
     */
    public Estoque getEstoque() {
        return estoque;
    }

    /**
     * @param estoque the estoque to set
     */
    public void setEstoque(Estoque estoque) {
        this.estoque = estoque;
    }
}
