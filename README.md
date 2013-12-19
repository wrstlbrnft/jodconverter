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
Here's a listing of the input to output conversions that we have tests automated for. Each input format/extension is listed, followed by the formats/extensions that my tests have successfully converted the input format/extension to.

_Notes:_
* _I performed these tests with LibreOffice version 4.1.3.2 on (K)ubuntu 13.10._
* _I did not manually verify the accuracy of each conversion, I only know these conversions saved and exited._

### General
<table>
	<tr>
		<th>source</th>
		<th>target</th>
	</tr>
	<tr>
		<td>csv</td>
		<td>csv, html, ods, pdf, sxc, tsv, xls, xlsx</td>
	</tr>
	<tr>
		<td>html</td>
		<td>doc, docx, html, odt, pdf, rtf, sxw, txt</td>
	</tr>
	<tr>
		<td>rtf</td>
		<td>doc, docx, html, odt, pdf, rtf, sxw, txt</td>
	</tr>
	<tr>
		<td>svg</td>
		<td>odg, pdf, swf, vdx, vsd, vsdm, vsdx</td>
	</tr>
	<tr>
		<td>tsv</td>
		<td>csv, html, ods, pdf, sxc, tsv, xls, xlsx</td>
	</tr>
	<tr>
		<td>txt</td>
		<td>doc, docx, html, odt, pdf, rtf, sxw, txt</td>
	</tr>
</table>

###LibreOffice, OpenOffice, StarOffice
<table>
	<tr>
		<th>source</th>
		<th>target</th>
	</tr>
	<tr>
		<td>odg</td>
		<td>odg, pdf, swf, vdx, vsd, vsdm, vsdx</td>
	</tr>
	<tr>
		<td>odp</td>
		<td>html, odp, pdf, ppt, swf, sxi</td>
	</tr>
	<tr>
		<td>ods</td>
		<td>csv, html, ods, pdf, sxc, tsv, xls, xlsx</td>
	</tr>
	<tr>
		<td>odt</td>
		<td>doc, docx, html, odt, pdf, rtf, sxw, txt</td>
	</tr>
	<tr>
		<td>sxc</td>
		<td>csv, html, ods, pdf, sxc, tsv, xls, xlsx</td>
	</tr>
	<tr>
		<td>sxi</td>
		<td>html, odp, pdf, ppt, swf, sxi</td>
	</tr>
	<tr>
		<td>sxw</td>
		<td>doc, docx, html, odt, pdf, rtf, sxw, txt</td>
	</tr>
</table>

### Microsoft
<table>
	<tr>
		<th>source</th>
		<th>target</th>
	</tr>
	<tr>
		<td>doc</td>
		<td>doc, docx, html, odt, pdf, rtf, sxw, txt</td>
	</tr>
	<tr>
		<td>docx</td>
		<td>doc, docx, html, odt, pdf, rtf, sxw, txt</td>
	</tr>
	<tr>
		<td>ppt</td>
		<td>html, odp, pdf, ppt, swf, sxi</td>
	</tr>
	<tr>
		<td>vdx</td>
		<td>odg, pdf, swf, vdx, vsd, vsdm, vsdx</td>
	</tr>
	<tr>
		<td>vsd</td>
		<td>odg, pdf, swf, vdx, vsd, vsdm, vsdx</td>
	</tr>
	<tr>
		<td>vsdm</td>
		<td>odg, pdf, swf, vdx, vsd, vsdm, vsdx</td>
	</tr>
	<tr>
		<td>vsdx</td>
		<td>odg, pdf, swf, vdx, vsd, vsdm, vsdx</td>
	</tr>
	<tr>
		<td>xls</td>
		<td>csv, html, ods, pdf, sxc, tsv, xls, xlsx</td>
	</tr>
	<tr>
		<td>xlsx</td>
		<td>csv, html, ods, pdf, sxc, tsv, xls, xlsx</td>
	</tr>
</table>

#### Unexpected Failures
_I'm unable to save anything to svg format in my tests... not even a file that's already svg. The following conversions to svg have been attempted but have failed:_
* _odg > svg_
* _svg > svg_
* _vdx > svg_
* _vsd > svg_
* _vsdm > svg_
* _vsdx > svg_

## TODO
- copy and update documentation from Google Code Wiki pages
  - Getting Started
  - Configuration
  - Whats New in 3
  - FAQ
- figure out why we can't save anything to svg format
- finalize Visio to PDF support and provide tests

## Getting Started
[Google Code Getting Started Page](http://code.google.com/p/jodconverter/wiki/GettingStarted)

## Configuration
[Google Code Configuration Page](http://code.google.com/p/jodconverter/wiki/Configuration)

## Building from Source

### Checkout
Check out the source code and sample app from GitHub using your method of choice

SSH
```
git clone git@github.com:erictallman/jodconverter.git ~/projects/jodconverter
```

HTTPS
```
git clone https://github.com/erictallman/jodconverter.git ~/projects/jodconverter
```

Subversion
```
svn checkout https://github.com/erictallman/jodconverter ~/projects/jodconverter
```

or, [Download Zip](https://github.com/erictallman/jodconverter/archive/master.zip)


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
