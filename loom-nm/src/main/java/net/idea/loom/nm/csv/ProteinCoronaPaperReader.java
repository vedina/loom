package net.idea.loom.nm.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.exception.CDKException;

import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.core.io.IteratingDelimitedFileReaderComplexHeader;
import ambit2.core.io.StringArrayHeader;

public class ProteinCoronaPaperReader extends IteratingDelimitedFileReaderComplexHeader<StringArrayHeader> {

	public ProteinCoronaPaperReader(InputStream in)
			throws UnsupportedEncodingException, CDKException {
		super(in);
		setNumberOfHeaderLines(6);
	}
	public ProteinCoronaPaperReader(Reader reader)  throws CDKException {
		super(reader);
		setNumberOfHeaderLines(6);
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
			int col = 0;
			for (String token : tokens) {
				if (nline==0)
					addHeaderColumn(token);
				else {
					StringArrayHeader column = getHeaderColumn(col);
					column.setValue(nline, token);
				}
				col++;
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
	protected StringArrayHeader createPropertyByColumnName(String name) {
		return new StringArrayHeader("PRCR-",getNumberOfHeaderLines(),name) {
			@Override
			protected ProtocolApplication<Protocol, Params, String, Params, String> getExperiment(Protocol protocol) {
				ProtocolApplication<Protocol, Params, String, Params, String> exp = new ProtocolApplication<Protocol, Params, String, Params, String>(protocol);
				exp.setReferenceYear("2014");
				exp.setReference("Protein Corona Fingerprinting Predicts the Cellular Interaction of Gold and Silver Nanoparticles");
				return exp;
			}
		};
	}
	
}

