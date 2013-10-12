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
* O executável ssh precisa estar instalado e o executável sshd precisa estar em execução para utilizar-se os script do Hadoop
* A última distribuição estável do Hadoop (suporta Win32 para desenvolvimento)
 
Windows
* Cygwin

##1. Instalação

Navegue até o diretório desejado como destino da instalação do DBpedia Spotlight. Execute os seguintes comandos:

```
git clone https://github.com/dbpedia-spotlight/dbpedia-spotlight.git
cd dbpedia-spotlight/
mvn install
```

(falar ou linkar para a instalação do Java)
(falar ou linkar para a instalação do ssh)
(falar ou linkar para a instalação do Hadoop)

##1. Baixando os dados

Para baixar todos os dados necessários, basta executar o shell script [download.sh](https://github.com/Zaknarfen/dbpedia-spotlight/blob/master/bin/download.sh) que reside na pasta dbpedia-spotlight/bin.

```
usage()
{
     echo "download.sh"
     echo "Parameters: "
     echo "1) Spotlight workspace (example /home/ubuntu/Spotlight)"
     echo "2) Main language abbreviation (example en)"
     echo "3) Complement languages to improve the indexing stage if desired (example 'it fr pt')"
     echo " "
     echo "Usage: ./download.sh /home/ubuntu/Spotlight en"
     echo "Downloads all the needed files for the indexing process."
     echo " "
}
```

##2. Indexando os dados

Em seguida é necessário executar o shell script [index_db.sh](https://github.com/Zaknarfen/dbpedia-spotlight/blob/master/bin/index_db.sh) para indexar os dados baixados. Ambos os scripts estão no mesmo diretório.

(continua aqui)


