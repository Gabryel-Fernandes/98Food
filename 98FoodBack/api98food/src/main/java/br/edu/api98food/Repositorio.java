package br.edu.api98food;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Repositorio<T extends Identificavel> {
    private final String entidade;
    private final File arquivo;
    private List<T> dados = new ArrayList<>();

    public Repositorio(String entidade, String caminho) {
        this.entidade = entidade;
        this.arquivo = new File(caminho);
    }


    @SuppressWarnings("unchecked")
    public void carregar() throws PersistenciaException {
        if (!arquivo.exists()) {
            dados = new ArrayList<>();
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(arquivo))) {
            dados = (ArrayList<T>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Erro ao ler " + arquivo.getName(), e);
        }
    }

    public void salvar() throws PersistenciaException {
        File pai = arquivo.getParentFile();
        if (pai != null) pai.mkdirs();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            out.writeObject(new ArrayList<>(dados));
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao gravar " + arquivo.getName(), e);
        }
    }


    public void incluir(T obj) throws DadoInvalidoException {
        for (T t : dados)
            if (t.getId() == obj.getId())
                throw new DadoInvalidoException("ID duplicado em " + entidade + ": " + obj.getId());
        dados.add(obj);
    }

    public void alterar(T obj) throws RegistroNaoEncontradoException {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getId() == obj.getId()) {
                dados.set(i, obj);
                return;
            }
        }
        throw new RegistroNaoEncontradoException(entidade, obj.getId());
    }

    public void excluir(int id) throws RegistroNaoEncontradoException {
        dados.remove(consultar(id));
    }

    public T consultar(int id) throws RegistroNaoEncontradoException {
        for (T t : dados)
            if (t.getId() == id) return t;
        throw new RegistroNaoEncontradoException(entidade, id);
    }

    public List<T> listar() {
        return new ArrayList<>(dados);
    }

    public int maiorId() {
        int max = 0;
        for (T t : dados) max = Math.max(max, t.getId());
        return max;
    }
}