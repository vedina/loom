package net.idea.loom.nm.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.idea.i5.io.I5CONSTANTS;
import net.idea.i5.io.I5_ROOT_OBJECTS;
import ambit2.base.data.Property;
import ambit2.base.data.StructureRecord;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.study.ReliabilityParams;
import ambit2.base.data.study.Value;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.STRUCTURE_RELATION;
import ambit2.base.relation.composition.Proportion;
import ambit2.core.io.StringArrayHeader;

public class ProteinCoronaCSVHeader extends StringArrayHeader<I5_ROOT_OBJECTS> {
	enum _lines {
		endpointcategory,
		technology,
		endpoint,
		cell,
		medium,
		result,
		units
	}
	
	public ProteinCoronaCSVHeader(String prefix,int nlines) {
		super(prefix,nlines);
	}
	public ProteinCoronaCSVHeader(String prefix,int nlines,String value) {
		super(prefix,nlines,value);
	}
	@Override
	public void assign(SubstanceRecord record, Object value) {
		if ("Change".equals(lines[_lines.medium.ordinal()])) return;
		else if ("Designation".equals(lines[_lines.result.ordinal()])) {
			if (value == null) {
				record.setPublicName(null);
				record.setCompanyName(null);
				record.setCompanyUUID(prefix+UUID.randomUUID());
			} else {
				record.setPublicName(value.toString());
				record.setCompanyName(value.toString());
				record.setCompanyUUID(prefix+UUID.nameUUIDFromBytes(value.toString().getBytes()));
			}
			record.setSubstancetype("nanoparticle");
		} else if ("Classification".equals(lines[_lines.endpoint.ordinal()])) {
				if (value != null) {
					List<ExternalIdentifier> ids = new ArrayList<ExternalIdentifier>();
					ids.add(new ExternalIdentifier(lines[_lines.technology.ordinal()],value.toString()));
					ids.add(new ExternalIdentifier("DOI","http://dx.doi.org/10.1021/nn406018q"));
					record.setExternalids(ids);
				}
		} else if ("Core composition".equals(lines[_lines.endpoint.ordinal()])) {
			if ("Element".equals(lines[_lines.medium.ordinal()])) {
				//get the protocol to characterize composition from the paper!
				Protocol protocol = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY.getProtocol(lines[_lines.technology.ordinal()]);
				protocol.addGuideline(lines[_lines.technology.ordinal()]);

				ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = getExperiment(I5_ROOT_OBJECTS.SURFACE_CHEMISTRY,record,protocol);
				
				EffectRecord<String,IParams,String> effect = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY.createEffectRecord();
				effect.setEndpoint("ATOMIC COMPOSITION");
				effect.setTextValue(value.toString());
				//effect.getConditions().put("ELEMENT_OR_GROUP", new Value(value));
				effect.getConditions().put("TYPE", new Value("CORE"));
				experiment.addEffect(effect);
				
				UUID uuid = UUID.nameUUIDFromBytes(value.toString().getBytes());
				record.setReferenceSubstanceUUID(prefix+uuid.toString());
				IStructureRecord core = new StructureRecord();
				record.addStructureRelation(record.getCompanyUUID(), core, STRUCTURE_RELATION.HAS_CORE, new Proportion());

				try {core.setProperty(Property.getNameInstance(),value.toString());} catch (Exception x) {};
				try {core.setContent(value.toString()); core.setFormat("INC"); core.setSmiles(core.getContent());} catch (Exception x) {};
				try {core.setProperty(Property.getI5UUIDInstance(),prefix+uuid.toString());} catch (Exception x) {};

			}
		} else if ("Surface modifier".equals(lines[_lines.endpoint.ordinal()])) {
			if ("Abbreviated".equals(lines[_lines.medium.ordinal()])) {
				//get the protocol to characterize composition from the paper!
				Protocol protocol = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY.getProtocol(lines[_lines.technology.ordinal()]);
				protocol.addGuideline(lines[_lines.technology.ordinal()]);
				
				ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = getExperiment(I5_ROOT_OBJECTS.SURFACE_CHEMISTRY,record,protocol);

				EffectRecord<String,IParams,String> effect = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY.createEffectRecord();
				effect.setEndpoint("FUNCTIONAL GROUP");
				effect.setTextValue(value.toString());
				//effect.getConditions().put("ELEMENT_OR_GROUP", new Value(value));
				effect.getConditions().put("TYPE", new Value("FUNCTIONALIZATION"));
				experiment.addEffect(effect);
				
				IStructureRecord coating = new StructureRecord();
				record.addStructureRelation(record.getCompanyUUID(), coating, STRUCTURE_RELATION.HAS_COATING, new Proportion());
				//try {record.setOwnerName();} catch (Exception x) {};
				try {coating.setProperty(Property.getNameInstance(),value);} catch (Exception x) {};
				try {coating.setProperty(Property.getI5UUIDInstance(),prefix+UUID.nameUUIDFromBytes(value.toString().getBytes()));} catch (Exception x) {};
			}
		} else if ("LC MS/MS".equals(lines[_lines.technology.ordinal()])) {
			Double num = Double.parseDouble(value.toString());
			if (num>0) {
				I5_ROOT_OBJECTS category = I5_ROOT_OBJECTS.UNKNOWN_TOXICITY; 
				try {
					category = category.valueOf(lines[_lines.endpointcategory.ordinal()]);
				} catch (Exception x) {}
				Protocol protocol = category.getProtocol(lines[_lines.technology.ordinal()]);
				protocol.addGuideline("Solution-based digestion protocol for serum protein isolates");
				ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = null;
				experiment = getExperiment(category,record,protocol);
				experiment.getParameters().put("Type of method", lines[_lines.technology.ordinal()]);
				experiment.getParameters().put(I5CONSTANTS.pDATA_GATHERING_INSTRUMENTS,"Orbitrap-Velos mass spectrometer (Thermo)");
				EffectRecord<String,IParams,String> effect = null;
				if (experiment.getEffects()==null || experiment.getEffects().size()==0) { 
					effect = getEffectRecord(category, experiment);
					effect.setEndpoint("Spectral counts");
					effect.setTextValue(new Params());
					experiment.addEffect(effect);
				} else  {
					effect = experiment.getEffects().get(0);
				}
				((Params)effect.getTextValue()).put(lines[_lines.result.ordinal()],new Value<Integer>(Integer.parseInt(value.toString()),null));
				//effect.setTextValue(lines[4]);


				
			}
		} else {
			String line = lines[_lines.result.ordinal()].toLowerCase();
			if (("".equals(line) 
					|| "mean".equals(line)
					|| "sd".equals(line)
					|| "std".equals(line)
					|| "sem".equals(line)
					|| "n".equals(line))
					) {
				I5_ROOT_OBJECTS category = I5_ROOT_OBJECTS.UNKNOWN_TOXICITY; 
				try {
					category = category.valueOf(lines[_lines.endpointcategory.ordinal()]);
				} catch (Exception x) {}
				Protocol protocol = category.getProtocol(lines[_lines.technology.ordinal()]);
				protocol.addGuideline(lines[_lines.technology.ordinal()]);
				
				ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = getExperiment(category,record,protocol);
				
				experiment.getParameters().put("Type of method", lines[_lines.technology.ordinal()]);
				EffectRecord<String,IParams,String> effect = getEffectRecord(category, experiment);
				if ("mean".equals(line) || "".equals(line) ) {
					String endpoint = lines[_lines.endpoint.ordinal()];
					effect.setEndpoint(endpoint.length()>45?endpoint.substring(0,45):endpoint);
					effect.getConditions().put("MEDIUM", getMedium(lines[_lines.medium.ordinal()]));
					
					if (endpoint.toLowerCase().indexOf("mean")<0)
						effect.setLoQualifier(line);
					effect.setLoValue(Double.parseDouble(value.toString()));
					effect.setUnit(lines[_lines.units.ordinal()]==null?null:lines[_lines.units.ordinal()].trim());
					experiment.addEffect(effect);

				} else if ("sd".equals(line) || "std".equals(line)  || "sem".equals(line)) {
					try {
						effect.setStdDev(Double.parseDouble(value.toString()),lines[_lines.units.ordinal()]==null?null:lines[_lines.units.ordinal()].trim());
					} catch (Exception x) {
						effect.setStdDev((Double)null);
					}
				} else if ("n".equals(line)) {
					try {
						Value n = new Value<Integer>(Integer.parseInt(value.toString()));
						n.setLoQualifier("=");
						effect.getConditions().put("N",n);
					} catch (Exception x) {
						effect.getConditions().put("N",null);
					}
				}
				
			}
		}	
	
		}
	protected EffectRecord<String,IParams,String> getEffectRecord(I5_ROOT_OBJECTS category, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment) {
		String sampleUUID = prefix+UUID.nameUUIDFromBytes(
				(	
				lines[_lines.endpointcategory.ordinal()]+
				lines[_lines.technology.ordinal()]+
				lines[_lines.endpoint.ordinal()]+
				lines[_lines.medium.ordinal()]
				      ).getBytes()
		);		
		if (experiment.getEffects()!=null)
			for (EffectRecord<String,IParams,String> effect : experiment.getEffects()) 
				if (sampleUUID.equals(effect.getSampleID())) {
					return effect;
				}
		EffectRecord<String,IParams,String> effect = category.createEffectRecord();
		effect.setSampleID(sampleUUID);
		return effect;
	}
	protected ProtocolApplication<Protocol, IParams, String, IParams, String> getExperiment(
			I5_ROOT_OBJECTS category, SubstanceRecord record,Protocol protocol) {
		String experimentUUID = prefix+UUID.nameUUIDFromBytes(
				(record.getCompanyUUID() +
						category.name() + 
						lines[_lines.technology.ordinal()]
				                                 //+lines[_lines.endpoint.ordinal()]
				                                        ).getBytes()
				);
		
		if (record.getMeasurements()!=null)
			for (ProtocolApplication<Protocol, IParams, String, IParams, String> experiment : record.getMeasurements()) {
				if (experimentUUID.equals(experiment.getDocumentUUID())) return experiment;
			}
		ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = category.createExperimentRecord(protocol);
		ReliabilityParams reliability = new ReliabilityParams();
		reliability.setStudyResultType("experimental result");
		experiment.setReliability(reliability);
		experiment.setParameters(new Params());
		experiment.getParameters().put("testmat_form", null);
		experiment.getParameters().put("Type of method",null);
		setCitation(experiment);
		experiment.setDocumentUUID(experimentUUID);
		record.addMeasurement(experiment);
		return experiment;
	}
	@Override
	protected void setCitation(
			ProtocolApplication<Protocol, IParams, String, IParams, String> experiment) {
		experiment.setReferenceYear("2014");
		experiment.setReference("Protein Corona Fingerprinting Predicts the Cellular Interaction of Gold and Silver Nanoparticles");
	}
	@Override
	protected String getMedium(String cell) {
		String medium = null;
		if ((cell!=null) && cell.indexOf("serum")>0) {
			medium = "Human serum";
		} return medium;
	}
	
}