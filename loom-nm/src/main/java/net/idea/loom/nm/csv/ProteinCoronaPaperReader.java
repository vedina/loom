package net.idea.loom.nm.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.exception.CDKException;

import ambit2.base.data.ILiteratureEntry._type;
import ambit2.base.data.LiteratureEntry;
import ambit2.core.io.IteratingDelimitedFileReaderComplexHeader;
import ambit2.core.io.StringArrayHeader;

public class ProteinCoronaPaperReader extends IteratingDelimitedFileReaderComplexHeader<StringArrayHeader> {
	protected LiteratureEntry citation;
	public ProteinCoronaPaperReader(InputStream in,LiteratureEntry citation)
			throws UnsupportedEncodingException, CDKException {
		super(in);
		setNumberOfHeaderLines(7);
		this.citation = citation;
	}
	public ProteinCoronaPaperReader(Reader reader)  throws CDKException {
		this(reader,new LiteratureEntry("Protein Corona","http://dx.doi.org/10.1021/nn406018q"));
	}
	public ProteinCoronaPaperReader(Reader reader,LiteratureEntry citation)  throws CDKException {
		super(reader);
		setNumberOfHeaderLines(7);
		this.citation = citation;
	}

	@Override
	protected void processHeader(BufferedReader in) {
		for (int i=0; i < getNumberOfHeaderLines();i++) {
			processHeader(in,i);		
		}	
	}
	
	protected void processHeader(BufferedReader in, int nline) {
		try {
			
			String line = in.readLine();
			while (line.startsWith(commentChar)) {
				processComment(line);
				line = in.readLine();
			}
			String[] tokens = StringUtils.splitPreserveAllTokens(line,new String(format.getFieldDelimiter()));
			for (int col = 0; col < tokens.length; col++) {
				String token = tokens[col];
				if (nline==0)
					addHeaderColumn(token);
				else {
					StringArrayHeader column = getHeaderColumn(col);
					column.setValue(nline, col, token);
				}
			}
			//no SMILES expected, if needed get code from super class
			if (nline==0)
				values = new Object[getNumberOfColumns()];
			
			
		} catch (IOException x) {
			logger.log(Level.SEVERE,x.getMessage(),x); 
		}
	}
    
	@Override
	protected String getSmilesHeader(int index) {
		return null;
	}

	@Override
	protected ProteinCoronaCSVHeader createPropertyByColumnName(String name) {
		ProteinCoronaCSVHeader column = new ProteinCoronaCSVHeader("PRCR-",getNumberOfHeaderLines(),name);
		column.setHeader(header);
		return column;
	}
	@Override
	protected LiteratureEntry getReference() {
		return citation;
	}
	
}

