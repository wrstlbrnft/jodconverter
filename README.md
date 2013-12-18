# JODConverter

JODConverter (for Java OpenDocument Converter) automates document conversions
using LibreOffice or Apache OpenOffice.

Forked from https://github.com/mirkonasato/jodconverter

There is documentation on [Google Code](http://code.google.com/p/jodconverter/). Over time I'll copy that over and update it as necessary so this can be a central point of reference.

## README Sections
- [Licensing](#licensing)
- [Version](#version)
- [Supported Conversions](#supported-conversions)
- [TODO](#todo)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Building From Source](#building-from-source)
- [What's New in 3](#whats-new-in-3)
- [FAQ](#faq)

## Licensing
JODConverter is open source software, you can redistribute it and/or
modify it under either (at your option) of the following licenses:

1. The [GNU Lesser General Public License, Version 3](LICENSE-LGPL3.txt) (or later)
2. The [Apache License, Version 2.0](LICENSE-APACHE2.txt)

Take a look at each individual dependency license to see if it's right for you.

## Version
3 Beta... since I'm new to this particular project, I suspect it will remain in Beta for the forseeable future.

## Supported Conversions
Listing by an application's file type that are currently supported and which output targets are available by default.
<table>
	<thead>
		<tr>
			<th colspan="2">Application, Vendor, etc</th>
		</tr>
		<tr>
			<th>Input File Type</th>
			<th>Output File Type</th>
		</tr>
	</thead>
	<tfoot>
		<tr>
			<td colspan="2">
				<div>&spades; - Limited testing</div>
			</td>
		</tr>
	</tfoot>
	<tbody>

		<tr><td colspan="2"></tr>
		<tr>
			<th colspan="2">Microsoft Visio<sup>&spades;</sup></th>
		</tr>
		<tr>
			<td>vdx</td>
			<td>pdf</td>
		</tr>
		<tr>
			<td>vsd</td>
			<td>pdf</td>
		</tr>
		<tr>
			<td>vsdm</td>
			<td>pdf</td>
		</tr>
		<tr>
			<td>vsdx</td>
			<td>pdf</td>
		</tr>

		<tr><td colspan="2"></tr>
		<tr>
			<th colspan="2">... more complete listing coming ...</th>
		</tr>

	</tbody>
</table>

## TODO
- copy and update documentation from Google Code Wiki pages
  - Getting Started
  - Configuration
  - Building from Source
  - Whats New in 3
  - FAQ
- compile comprehensive list of default supported conversions
- finalize Visio to PDF support and provide tests

## Getting Started
[Google Code Getting Started Page](http://code.google.com/p/jodconverter/wiki/GettingStarted)

## Configuration
[Google Code Configuration Page](http://code.google.com/p/jodconverter/wiki/Configuration)

## Building from Source

### Checkout
Check out the source code and sample app from GitHub using your method of choice
- [SSH](git@github.com:erictallman/jodconverter.git)
- [HTTPS](https://github.com/erictallman/jodconverter.git)
- [Subversion](https://github.com/erictallman/jodconverter)
- [Download Zip](https://github.com/erictallman/jodconverter/archive/master.zip)

For this documentation we'll assume you have checked it out into:
```
~/projects/jodconverter
```

The source code is located in:
```
~/projects/jodconverter/jodconverter-core
```

The sample app is located in:
```
~/projects/jodconverter/jodconverter-sample-webapp
```

### Maven Build
JODConverter uses Maven 3 as its build tool, so it can be built in the usual Maven way. A Sigar native library is now required for building (although it's optional at runtime).

Please see the [Maven](http://maven.apache.org/) website for more information about Maven.

#### Get Sigar
Since we need Sigar for the build process, download the right one for your system from [svn.hyperic.org](http://svn.hyperic.org/projects/sigar_bin/dist/SIGAR_1_6_5/lib/) and save it to a local dir. 
```
svn checkout http://svn.hyperic.org/projects/sigar_bin/dist/SIGAR_1_6_5/lib/ ~/projects/sigar1.6.5
```

#### Build Options
Now that you have Sigar, you'll need to specify it in your build command.
Example:
```
cd ~/projects/jodconverter/jodconverter-core
mvn -Djava.library.path=~/projects/sigar1.6.5 clean install
```
You can specify the LibreOffice/OpenOffice installation path explicity using the -Doffice.home flag.
Example:
```
cd ~/projects/jodconverter/jodconverter-core
mvn -Djava.library.path=~/projects/sigar1.6.5 -Doffice.home=/usr/lib64/libreoffice clean install
```

If you experience test failures, try skipping the tests (not a good practice but because of OOo integration failures may be due to timeout values rather than real issues):
Example:
```
cd ~/projects/jodconverter/jodconverter-core
mvn -Djava.library.path=~/projects/sigar1.6.5 -Doffice.home=/usr/lib64/libreoffice -DskipTests clean install
```

After you've built and tested everything out, you can roll a distribution zip file, containing the JODConverter library and dependencies, and be on your way.
Example:
```
cd ~/projects/jodconverter/jodconverter-core
mvn assembly:single
```

Want to build and roll a distribution all in one command? You can do that too!
Example:
```
cd ~/projects/jodconverter/jodconverter-core
mvn -Djava.library.path=~/projects/sigar1.6.5 -Doffice.home=/usr/lib64/libreoffice -DskipTests clean install assembly:single
```
which should result in a file called jodconverter-core-3.0-SNAPSHOT-dist.zip in:
```
~/projects/jodconverter/jodconverter-core/target
```


Original: [Google Code Building from Source Page](http://code.google.com/p/jodconverter/wiki/BuildingFromSource)

## What's New in 3
[Google Code What's New in Version 3.0 Page](http://code.google.com/p/jodconverter/wiki/WhatsNewInVersion3)

## FAQ
[Google Code FAQ Page](http://code.google.com/p/jodconverter/wiki/FAQ)
