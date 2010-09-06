#!/usr/bin/ruby

require 'fileutils'

# A script that attempts to merge the latest commons-net and sshj trunk 

# Remove packages we dont care about
["ntp","mail","nntp","time","unix","pop3","smtp","telnet","finger","bsd","chargen","whois","daytime","discard","echo"].each do |f|
	srcDir="src/main/java/org/apache/commons/net/" << f
	testDir="src/test/java/org/apache/commons/net/" << f
	examplesDir="src/main/java/examples/" << f
	
	[srcDir,testDir,examplesDir].each do |d|
		if File.exist? d
			puts "Removing dir #{d}"
			FileUtils.rm_rf d 
		end
	end
end

# Replace package declarations
def fixup_declarations(from,to) 
	Dir.glob("**/*.java").each do |srcFile|
		puts "Processing #{srcFile}"
		text = File.read(srcFile)
		new_text = text.gsub(from, to)
		File.open(srcFile, "w") {|file| file.puts new_text}		
	end
end

fixup_declarations(/org\.apache\.commons\.net/,"org\.netling")

# Move source trees
def move_src_tree(srcTree, dstTree) 
	["main","test"].each do |s|
		from = "src/" << s << srcTree
		to = "src/" << s << dstTree
		puts "Renaming: #{from} => #{to}"
		FileUtils.cp_r(from,to)
		puts "Removing " << from 
		FileUtils.rm_rf(from)
	end
end

move_src_tree("/java/org/apache/commons/net", "/java/org/netling")

# Rename/remove some files
File.rename("LICENSE.txt", "LICENSE")
File.delete("NOTICE.txt")
File.delete("doap_net.rdf")
File.delete("RELEASE-NOTES.txt")

# Add README
File.open("README.md","w") {|f|
	f.puts <<HERE
netling - Network protocols in Java
====================================

Building
--------

To build, you must have Maven 2 installed. Maven can be downloaded from http://maven.apache.org/.

Documentation
-------------

You can build the documentation with:

    mvn site
HERE
}

# Add NOTICE
File.open("NOTICE", "w") {|f| 
	f.puts <<HERE
netling - Network protocols in Java
------------------------------------

Copyright 2010 netling contributors
Copyright 2001-2010 The Apache Software Foundation
Copyright 1996-2001 Daniel F. Savarese

This product includes code derived from software developed at
The Apache Software Foundation (http://www.apache.org/):

- Apache Commons-Net
- Apache MINA SSHD


// Apache Commons Net notice

Apache Commons Net
Copyright 2001-2010 The Apache Software Foundation

This product includes software developed by
The Apache Software Foundation (http://www.apache.org/).


// Apache Mina SSHD notice

   =========================================================================
   == NOTICE file for use with the Apache License, Version 2.0, ==
   == in this case for the SSHD distribution. ==
   =========================================================================

   This product contains software developped by JCraft,Inc. and subject to
   the following license:

Copyright (c) 2002,2003,2004,2005,2006,2007,2008 Atsuhiko Yamanaka, JCraft,Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--------------------------------------------------------------------------------
Copyright (c) 2000 - 2006 The Legion Of The Bouncy Castle (http://www.bouncycastle.org)

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in the
Software without restriction, including without limitation the rights to use, copy,
modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to the
following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
HERE
}

def replace_license(fileglob, pattern, replacement)
	# Replace license with netling license
	Dir.glob(fileglob).each do |srcFile|
        	puts "Processing #{srcFile}"
        	text = File.read(srcFile)
		text.gsub!(pattern, replacement)
		File.open(srcFile,"w") {|f| f.puts text }
	end
end

netling_license=<<HERE
/*
* Copyright 2010 netling project <http://netling.org>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
HERE

replace_license("**/*.java", /(\/.*\* Licensed.*?\*\/)/m , netling_license)

# Delete xdocs we dont need
Dir.glob("src/site/xdoc/*.xml").each do |f|
	File.delete f
end

# Replace license in remaining xml files
        netling_xml_license=<<HERE
<!--
* Copyright 2010 netling project <http://netling.org>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
-->
HERE

replace_license("**/*.xml", /(<!\-\-.*Licensed.*?\-\->)/m, netling_xml_license)

# Replace pom with our one
File.open("pom.xml","w") {|pom|
	pom.puts <<HERE

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.netling</groupId>
    <artifactId>netling</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Netling</name>
    <description>
        Network protocols in Java
    </description>
    <url>https://github.com/shikhar/netling</url>
    <issueManagement>
        <system>jira</system>
        <url>https://github.com/shikhar/netling/issues</url>
    </issueManagement>
    <inceptionYear>2001</inceptionYear>

    <scm>
        <connection>scm:git:git://github.com/shikhar/netling.git</connection>
        <developerConnection>scm:git:git@github.com:shikhar/netling.git</developerConnection>
        <url>http://github.com/shikhar/netling</url>
    </scm>

    <licenses>
        <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
        </license>
    </licenses>

    <developers>
        <developer>
            <name>Rory Winston</name>
            <id>rwinston</id>
            <email>rwinston@apache.org</email>
            <organization/>
        </developer>
        <developer>
            <name>Shikhar Bhushan</name>
            <id>shikhar</id>
            <email>shikhar@schmizz.net</email>
            <organization/>
        </developer>
    </developers>

    <dependencies>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.6.1</version>
        </dependency>
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk16</artifactId>
            <version>1.45</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.8.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.jcraft</groupId>
            <artifactId>jzlib</artifactId>
            <version>1.0.7</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.sshd</groupId>
            <artifactId>sshd-core</artifactId>
            <version>0.4.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
            <version>0.9.24</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>0.9.24</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <plugins>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.6</source>
                    <target>1.6</target>
                    <excludes>
                        <exclude>**/examples/**/*.java</exclude>
                    </excludes>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>**/*FunctionalTest.java</exclude>
                        <exclude>**/*POP3*.java</exclude>
                        <exclude>**/TestSetupParameters.java</exclude>
                    </excludes>
                </configuration>
            </plugin>

            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
		<version>2.2-beta-5</version>
                <configuration>
                    <descriptors>
                        <descriptor>src/assembly/bin.xml</descriptor>
                        <descriptor>src/assembly/src.xml</descriptor>
                    </descriptors>
                    <tarLongFileMode>gnu</tarLongFileMode>
                </configuration>
            </plugin>
	</plugins>

        <testResources>
            <testResource>
                <directory>${basedir}/src/test/java</directory>
                <includes>
                    <include>**/*Test.java</include>
                </includes>
            </testResource>
        </testResources>

    </build>

    <profiles>
        <profile>
            <id>full-deps</id>
            <dependencies>
                <dependency>
                    <groupId>org.bouncycastle</groupId>
                    <artifactId>bcprov-jdk16</artifactId>
                    <version>1.45</version>
                </dependency>
                <dependency>
                    <groupId>com.jcraft</groupId>
                    <artifactId>jzlib</artifactId>
                    <version>1.0.7</version>
                </dependency>
                <dependency>
                    <groupId>ch.qos.logback</groupId>
                    <artifactId>logback-core</artifactId>
                    <version>0.9.24</version>
                </dependency>
                <dependency>
                    <groupId>ch.qos.logback</groupId>
                    <artifactId>logback-classic</artifactId>
                    <version>0.9.24</version>
                </dependency>
            </dependencies>
        </profile>
    </profiles>

    <reporting>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>findbugs-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </reporting>

</project>
HERE
}

# Now pull in Shikhar's sshj changes, assuming it is checked out in ../sshj
FileUtils.cp_r("../sshj/src/",".")

# Change package declarations
fixup_declarations(/net\.schmizz/ ,"org\.netling")

move_src_tree("/java/net/schmizz/sshj", "/java/org/netling")
move_src_tree("/java/net/schmizz/concurrent", "/java/org/netling")
	
replace_license("**/*.java", /(\/.*\* Copyright 2010 Shikhar.*?\*\/)/m ,netling_license)

