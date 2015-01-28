
#Install Maven
mkdir /usr/share/maven
cd /usr/share/maven
wget http://mirrors.koehn.com/apache/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
tar xvzf apache-maven-3.2.5-bin.tar.gz
ln -s /usr/share/maven/apache-maven-3.2.5/ /usr/share/maven/latest
echo 'M2_HOME=/usr/share/maven/latest' >> ~/.bashrc
echo 'M2=$M2_HOME/bin' >> ~/.bashrc
echo 'PATH=$PATH:$M2' >> ~/.bashrc
export M2_HOME=/usr/share/maven/latest
export M2=$M2_HOME/bin
export PATH=$PATH:$M2


#Pull latest code/sample documents and copy Solr and 'Doc Crawler' Ambari stack to the services dir
cd
git clone https://github.com/abajwa-hw/search-demo.git	
cp -R ~/search-demo/doc_crawler_stack /var/lib/ambari-server/resources/stacks/HDP/2.2/services/
cp -R ~/search-demo/solr_stack /var/lib/ambari-server/resources/stacks/HDP/2.2/services/

#Compile the view and copy jar to Ambari views dir
cd ~/search-demo/doc_crawler_view

#Tell maven to compile against ambari jar
mvn install:install-file -Dfile=/usr/lib/ambari-server/ambari-views-1.7.0.169.jar -DgroupId=org.apache.ambari -DartifactId=ambari-views -Dversion=1.3.0-SNAPSHOT -Dpackaging=jar

#Compile view
mvn clean package

#move jar to Ambari dir
cp target/*.jar /var/lib/ambari-server/resources/views

#on HDP 2.2 sandbox
service ambari restart

#on other HDP 2.2 setups
#service ambari-server restart

echo "Installed Solr/Document Viewer services."
echo "Now open Ambari, add Solr service, then add 'Dcoument Crawler' service and finally open 'Document Crawler' view in Ambari"
