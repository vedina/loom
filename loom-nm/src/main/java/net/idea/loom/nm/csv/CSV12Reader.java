package net.idea.loom.nm.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.exception.CDKException;

import ambit2.base.data.LiteratureEntry;
import ambit2.core.io.IteratingDelimitedFileReaderComplexHeader;
import ambit2.core.io.StringArrayHeader;

public class CSV12Reader extends IteratingDelimitedFileReaderComplexHeader<StringArrayHeader> {
    protected LiteratureEntry citation;
    protected String prefix;

    public String getPrefix() {
	return prefix;
    }

    public void setPrefix(String prefix) {
	this.prefix = prefix;
    }

    public CSV12Reader(InputStream in, LiteratureEntry citation, String prefix) throws UnsupportedEncodingException,
	    CDKException {
	super(in);
	setNumberOfHeaderLines(CSV12Header._lines.values().length);
	this.citation = citation;
	this.prefix = prefix;
    }

    public CSV12Reader(File file) throws CDKException, FileNotFoundException {
	this(new FileReader(file), new LiteratureEntry(file.getName().toLowerCase(), file.getName().toLowerCase()),
		"FCSV-");
    }

    /*
     * this is now generic CSV reader public ProteinCoronaPaperReader(Reader
     * reader) throws CDKException { this(reader,new LiteratureEntry(
     * "Protein Corona Fingerprinting Predicts the Cellular Interaction of Gold and Silver Nanoparticles"
     * , "http://dx.doi.org/10.1021/nn406018q"),"PRCR-"); }
     */
    public CSV12Reader(Reader reader, LiteratureEntry citation, String prefix) throws CDKException {
	super(reader);
	setNumberOfHeaderLines(CSV12Header._lines.values().length);
	this.citation = citation;
	this.prefix = prefix;
    }

    @Override
    protected void processHeader(BufferedReader in) {
	for (int i = 0; i < getNumberOfHeaderLines(); i++) {
	    processHeader(in, i);
	}
    }

    protected void processHeader(BufferedReader in, int nline) {
	try {

	    String line = in.readLine();
	    while (line.startsWith(commentChar)) {
		processComment(line);
		line = in.readLine();
	    }
	    String[] tokens = StringUtils.splitPreserveAllTokens(line, new String(format.getFieldDelimiter()));
	    for (int col = 0; col < tokens.length; col++) {
		String token = tokens[col];
		if (nline == 0) {
		    addHeaderColumn(token);
		} else {
		    StringArrayHeader column = getHeaderColumn(col);
		    //System.out.println(col + ":" + column + " " + token);
		    column.setValue(nline, col, token);
		}
	    }
	    // no SMILES expected, if needed get code from super class
	    if (nline == 0)
		values = new Object[getNumberOfColumns()];

	} catch (IOException x) {
	    logger.log(Level.SEVERE, x.getMessage(), x);
	}
    }

    @Override
    protected String getSmilesHeader(int index) {
	return null;
    }

    @Override
    protected CSV12Header createPropertyByColumnName(String name) {
	CSV12Header column = new CSV12Header(prefix, getNumberOfHeaderLines(), name) {
	    protected void setCitation(
		    ambit2.base.data.study.ProtocolApplication<ambit2.base.data.study.Protocol, ambit2.base.data.study.IParams, String, ambit2.base.data.study.IParams, String> experiment) {
		experiment.setReferenceYear("2014");
		experiment.setReference(citation.getName());
	    };
	};
	column.setHeader(header);
	return column;
    }

    @Override
    protected LiteratureEntry getReference() {
	return citation;
    }

}
