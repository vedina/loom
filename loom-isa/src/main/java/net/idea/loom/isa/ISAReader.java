package net.idea.loom.isa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import org.isatools.isatab.ISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;

import uk.ac.ebi.bioinvindex.model.AssayResult;
import uk.ac.ebi.bioinvindex.model.Contact;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.CharacteristicValue;
import uk.ac.ebi.bioinvindex.model.term.FactorValue;
import uk.ac.ebi.bioinvindex.model.term.ParameterValue;
import ambit2.base.data.ILiteratureEntry;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.Value;
import ambit2.base.interfaces.ICiteable;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.IRawReader;

public class ISAReader extends DefaultIteratingChemObjectReader implements IRawReader<IStructureRecord>, ICiteable {
	protected SubstanceRecord record;
	protected BIIObjectStore store;
	
	
	public ISAReader(File directory) throws Exception {
		try {
			ISATABLoader loader = new ISATABLoader(directory.getAbsolutePath());
			FormatSetInstance isatabInstance = loader.load();

			ISATABValidator validator = new ISATABValidator(isatabInstance);
			
		    if (GUIInvokerResult.WARNING == validator.validate()) {
		         //vlog.warn("ISA-Configurator Validation reported problems, see the messages above or the log file");
		    }
		    store = validator.getStore();
		    parse();
		} catch (Exception x) {
			throw x;
		} finally {
			//anything to close?
		}
	}
	
	protected void parse() throws Exception {
		
		Collection<Identifiable> objects = new ArrayList<Identifiable>();
		/*
		Iterator<Class<? extends Identifiable>> i = store.types().iterator();
		while (i.hasNext()) {
			Class<? extends Identifiable> c = i.next();
			System.out.println("=================================");
			System.out.println(c.getName());
			
			objects.addAll(store.values(c));
	        for ( Identifiable object: objects ) {
	        	System.out.print("\t");
	        	System.out.println(object);
	        }
	        objects.clear();
		}
		*/
		objects.addAll(store.values(Study.class));
		Iterator<Identifiable> studyIterator = objects.iterator();
		while (studyIterator.hasNext()) {
			Identifiable object = studyIterator.next();
			Iterator<AssayResult> assayResultIterator = ((Study) object).getAssayResults().iterator();
			while (assayResultIterator.hasNext()) {
				AssayResult result = assayResultIterator.next();
				
				
				Protocol a_protocol = null; 
				for (Assay assay : result.getAssays()) {
					/*
					System.out.println(assay.getAcc());
					System.out.println(assay.getTechnologyName());
					System.out.println(assay.getTechnology());
					System.out.println(assay.getMeasurement());
					*/
					a_protocol = new Protocol(assay.getMeasurement().getName());
					a_protocol.setTopCategory("TOX");
					a_protocol.setCategory(assay.getTechnology().getName());

				}
				ambit2.base.data.study.ProtocolApplication a_papp = new ambit2.base.data.study.ProtocolApplication(a_protocol);
				a_papp.setReference(result.getStudy().getTitle());
				try {
					Calendar calendar = Calendar.getInstance();  
			        calendar.setTime(result.getStudy().getReleaseDate());  
					a_papp.setReferenceYear(Integer.toString(calendar.get(Calendar.YEAR)));
				} catch (Exception x) {}

				for (Contact contact : result.getStudy().getContacts()) {
					a_papp.setReferenceOwner(contact.getUrl());
				}
				Params params = new Params();
				a_papp.setParameters(params);
				
				Params conditions = new Params();
				trackAssayResult(result.getData().getProcessingNode(),a_protocol,params,conditions);		
				
				
				EffectRecord effect = new EffectRecord();
				effect.setEndpoint(result.getData().getName());
				effect.setTextValue(result.getData().getUrl());
				
				effect.setConditions(conditions);
				//System.out.println(result.getStudy());
				a_papp.addEffect(effect);
				System.out.println(a_papp);
			}
		}

		
	}
	
	protected void processFactorValues(Collection<FactorValue> factorvalues, Params params) {
		for (FactorValue pv : factorvalues) {
			Value factor = new Value();
			try {
				if (pv.getUnit()!=null)
					factor.setUnits(pv.getUnit().getValue());
			} catch (Exception x) {}
			try {
				factor.setLoValue(Double.parseDouble(pv.getValue()));
			} catch (Exception x) {
				factor.setLoValue(pv.getValue());
			}
			params.put(pv.getType().getValue(),factor);				
		}
	}
	protected void processCharacteristicValues(Collection<CharacteristicValue> characteristicValues, Params params) {
		for (CharacteristicValue pv : characteristicValues) {
			Value value = new Value();
			try {
				if (pv.getUnit()!=null)
					value.setUnits(pv.getUnit().getValue());
			} catch (Exception x) {}
			try {
				value.setLoValue(Double.parseDouble(pv.getValue()));
			} catch (Exception x) {
				value.setLoValue(pv.getValue());
			}
			params.put(pv.getType().getValue(),value);					
		}
	}	
	protected void processParamValues(Collection<ParameterValue> paramValues, Params params) {
		for (ParameterValue pv : paramValues) {
			Value value = new Value();
			try {
				if (pv.getUnit()!=null)
					value.setUnits(pv.getUnit().getValue());
			} catch (Exception x) {}
			try {
				value.setLoValue(Double.parseDouble(pv.getValue()));
			} catch (Exception x) {
				value.setLoValue(pv.getValue());
			}
			params.put(pv.getType().getValue(),value);					
		}
	}		
	protected void trackAssayResult(uk.ac.ebi.bioinvindex.model.processing.Node node,Protocol a_protocol,Params protocolParams,Params conditions) {
		if (node instanceof MaterialNode) {
			processFactorValues(((MaterialNode)node).getMaterial().getFactorValues(),conditions);
			processCharacteristicValues(((MaterialNode)node).getMaterial().getCharacteristicValues(),protocolParams);
		} else if (node instanceof DataNode) {
			processFactorValues(((DataNode)node).getData().getFactorValues(),conditions);
		} 		
			
		if (node.getDownstreamProcessings()==null) return;
		for (Object processing : node.getDownstreamProcessings()) 
			if (processing instanceof Processing) {
				Collection papps = ((Processing)processing).getProtocolApplications();
				if (papps!= null)
					for (Object p : papps) if (p instanceof ProtocolApplication) {
						//assay protocol params fo to conditions; study protocol params go to protocol parameters
						ProtocolApplication pa = (ProtocolApplication) p;
						boolean assayField = true;
						for (uk.ac.ebi.bioinvindex.model.Annotation annotation  : pa.getAnnotations()) {
							if ("sampleFileId".equals(annotation.getType().getValue())) {
								assayField = false; break;
							}
						}
						if (assayField) 
							processParamValues(pa.getParameterValues(),conditions);
						else
							processParamValues(pa.getParameterValues(),protocolParams);
						
						uk.ac.ebi.bioinvindex.model.Protocol protocol = ((ProtocolApplication)p).getProtocol();
						a_protocol.addGuideline(protocol.getName());
						//System.out.println(protocol);
						
					}
				for (Object in : ((Processing)processing).getInputNodes()) {
					trackAssayResult((uk.ac.ebi.bioinvindex.model.processing.Node)in,a_protocol,protocolParams,conditions);
				}
			}
	}
	protected void getResource(Identifiable node)  throws Exception {
		
	}
	@Override
	public void setReader(InputStream reader) throws CDKException {
		throw new CDKException("Not supported");
	}

	@Override
	public IResourceFormat getFormat() {
		return null;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILiteratureEntry getReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReference(ILiteratureEntry arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IStructureRecord nextRecord() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReader(Reader reader) throws CDKException {
		throw new CDKException("not supported");
	}


}
