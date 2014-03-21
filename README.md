scripts-globo
=============

## Sumário

Indexação da base de dados da Globo.

## Passo a passo

O primeiro passo para indexar uma base de dados utilizando o framework do DBpedia Spotlight é instalar as ferramentas necessárias para o funcionamento do framework.

##0. Requisitos

Linux / Windows
* A versão 0.6.5 do [DBpedia Spotlight](https://github.com/dbpedia-spotlight/dbpedia-spotlight/wiki/Installation) ou superior
* A versão 1.7 do Java ou superior
 
Windows
* Cygwin

##1. Instalação

Navegue até o diretório desejado como destino da instalação do DBpedia Spotlight. Execute os seguintes comandos:

```
git clone https://github.com/intrinsic-ltda/dbpedia-spotlight.git
cd dbpedia-spotlight/
mvn install
```

Navegue até o diretório desejado como destino da instalação do projeto que converte os dados da Globo para o formato esperado pelo DBpedia Spotlight. Execute os seguintes comandos:

```
git clone https://github.com/intrinsic-ltda/scripts-globo.git
cd dbpedia-spotlight/
mvn install
```

Se houver falha no segundo comando mvn install relativo a encoding, basta modificar o encoding do arquivo com problema para iso-8859-1. Isso pode ser feito pelo maven ou via IDE. Um exemplo de como isso é feito no IDEA Intellij está neste [link](http://www.jetbrains.com/idea/webhelp/configuring-individual-file-encoding.html).

##2. Baixando os dados da Globo

(falta linkar para o download dos arquivos da Globo)

Navegue até o diretório desejado para os arquivos da Globo. Execute os comandos:

```
mkdir globo_resources
cd globo_resources
mkdir turtle_files
mkdir graphs
mkdir TDB
mkdir output
wget FALTA_O_LINK
mv *.graph graphs/
mv *.ttl turtle_files/
```

Copy the DBpedia labels file into the Globo resources output folder, example:

```
cp /home/ubuntu/Spotlight/data/dbpedia/pt/labels_pt.nt.bz2 /home/ubuntu/globo_resources/output
```

Extract the file with bunzip2. Go to the output folder and execute the following command:

```
bunzip2 labels_pt.nt.bz2
```

##3. Baixando os arquivos da DBpedia

Para baixar todos os arquivos da DBpedia basta executar o shell script [download.sh](https://github.com/Zaknarfen/dbpedia-spotlight/blob/master/bin/download.sh) que reside na pasta dbpedia-spotlight/bin.

```
usage()
{
     echo "download.sh"
     echo "Parameters: "
     echo "1) Spotlight workspace (example /home/ubuntu/Spotlight)"
     echo "2) Main language abbreviation (example pt)"
     echo "3) Complement languages to improve the indexing stage if desired (example 'it fr pt')"
     echo " "
     echo "Usage: ./download.sh /home/ubuntu/Spotlight pt"
     echo "Downloads all the needed files for the indexing process."
     echo " "
}
```

##4. Gerando os arquivos de entrada para indexação a partir dos dados da Globo

Importe o projeto scripts-globo como projeto maven no IDEA Intellij. Com o projeto aberto, vá até a classe GenerateAllFiles. No menu principal da IDE vá em Run -> Edit Configurations.

Configue o VM options de acordo com a sua máquina e em Program Arguments indique a pasta raiz onde estão os arquivo de extensão turtle da Globo, como primeiro argumento, e o nome deste mesmo arquivo como segundo argumento separados por espaço. Por exemplo:

```
Program Arguments:     /home/globo_resources globo_dataset.ttl
```

Criado este Application basta executar o método main da classe GenerateAllFiles. Está classe irá criar diversos arquivos que serão utilizados para indexação. O processo pode demorar várias horas.

Ao final, navegue até a pasta de output (por default será raiz/output) e execute o comando:

```
cat surfaceForms-fromLabels-globo.tsv surfaceForms-fromOccs-globo.tsv > surfaceForms-globo.tsv
```

##5. Indexando os dados

Navegue até o diretório dbpedia-spotlight/bin e execute o shell script [index.sh](https://github.com/intrinsic-ltda/dbpedia-spotlight/blob/master/bin/index.sh)

Passe como parâmetros o diretório raíz de onde estão os arquivos da Globo e a língua abreviada, exemplo

```
./index.sh /home/ubuntu/globo_resources pt
```

##6. Acessando as APIs do DBpedia Spotlight

Para acessar as APIs do DBpedia Spotlight basta mandar uma requisição para o servidor de IP: 54.243.179.81. Para cada API utiliza-se um comando diferente.

Para acessar a API annotate basta executar o seguinte comando:

```
http://54.243.179.81:2222/rest/annotate?url=http://g1.globo.com/sp/campinas-regiao/noticia/2013/11/dilma-revela-preocupacao-com-saude-de-jose-genoino-na-prisao.html&coreferenceResolution=false
```

Caso exista necessidade da aplicação de algum filtro é necessário modificar o comando da seguinte forma:

```
http://54.243.179.81:2222/rest/annotate?url=http://g1.globo.com/sp/campinas-regiao/noticia/2013/11/dilma-revela-preocupacao-com-saude-de-jose-genoino-na-prisao.html&coreferenceResolution=false&types=DBpedia:Person
```

Para modificar o nível de confiança e o suporte basta modificar o comando da seguinte forma:

```
http://54.243.179.81:2222/rest/annotate?url=http://g1.globo.com/sp/campinas-regiao/noticia/2013/11/dilma-revela-preocupacao-com-saude-de-jose-genoino-na-prisao.html&coreferenceResolution=false&support=10&confidence=1&types=DBpedia:Person
```

Para acessar a API candidates basta executar o seguinte comando:

```
http://54.243.179.81:2222/rest/candidates?url=http://g1.globo.com/sp/campinas-regiao/noticia/2013/11/dilma-revela-preocupacao-com-saude-de-jose-genoino-na-prisao.html
```

