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
import ambit2.base.relation.composition.CompositionRelation;
import ambit2.base.relation.composition.Proportion;
import ambit2.core.io.StringArrayHeader;

public class CSV12Header extends StringArrayHeader<I5_ROOT_OBJECTS> {
	public enum _lines {
		endpointcategory {
			@Override
			public String getDescription() {
				StringBuilder b = new StringBuilder();
				b.append(super.getDescription());
				b.append(". One of :\n\n");
				b.append("<table>\n");
				for (I5_ROOT_OBJECTS root : I5_ROOT_OBJECTS.values()) if (root.isScientificPart()){
					b.append("<tr>");
					b.append("<td>");
					b.append(root.name());
					b.append("</td>");
					b.append("</tr>");
				}
				b.append("</table>");
				return b.toString();
			}
		},
		protocol,
		guideline,
		type_of_study,
		type_of_method,
		data_gathering_instruments,
		endpoint,
		condition1 {
			@Override
			public boolean isCondition() {
				return true;
			}
		},
		condition2 {
			@Override
			public boolean isCondition() {
				return true;
			}			
		},
		condition3 {
			@Override
			public boolean isCondition() {
				return true;
			}			
		},		
		result,
		units;
		public boolean isCondition() {
			return false;
		}
		public String getDescription() {
			return String.format("####Row\t%d \n%s",ordinal()+1,name()); 
		}
	}
	
	public CSV12Header(String prefix,int nlines) {
		super(prefix,nlines);
	}
	public CSV12Header(String prefix,int nlines,String value) {
		super(prefix,nlines,value);
	}
	@Override
	public void assign(SubstanceRecord record, Object value) {
		if ("Change".equals(lines[_lines.condition2.ordinal()])) return;
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
		} else if ("external identifier".equals(lines[_lines.endpointcategory.ordinal()].toLowerCase())) {
				if (value != null) {
					List<ExternalIdentifier> ids = new ArrayList<ExternalIdentifier>();
					ids.add(new ExternalIdentifier(lines[_lines.endpoint.ordinal()],value.toString()));
					record.setExternalids(ids);
				}
		} else if ("Chemical".equals(lines[_lines.endpointcategory.ordinal()])) {
			record.setSubstancetype("mono constituent substance");
			if ("SMILES".equals(lines[_lines.endpoint.ordinal()])) {
				
				UUID uuid = UUID.nameUUIDFromBytes(value.toString().getBytes());
				record.setReferenceSubstanceUUID(prefix+uuid.toString());
				IStructureRecord structure = new StructureRecord();
				record.addStructureRelation(record.getCompanyUUID(), structure, STRUCTURE_RELATION.HAS_CONSTITUENT, new Proportion());

				try {structure.setProperty(Property.getNameInstance(),value.toString());} catch (Exception x) {};
				try {structure.setContent(value.toString()); structure.setFormat("INC"); structure.setSmiles(structure.getContent());} catch (Exception x) {};
				try {structure.setProperty(Property.getI5UUIDInstance(),prefix+uuid.toString());} catch (Exception x) {};
				try {structure.setProperty(Property.getNameInstance(),record.getPublicName());} catch (Exception x) {};
				try {structure.setProperty(Property.getTradeNameInstance("Name"),record.getCompanyName());} catch (Exception x) {};
				
			} else if ("CASRN".equals(lines[_lines.endpoint.ordinal()])) {
				record.setPublicName(value.toString());
				if (record.getRelatedStructures()!=null)
					for (CompositionRelation rel : record.getRelatedStructures()) 
						if (STRUCTURE_RELATION.HAS_CONSTITUENT.name().equals(rel.getRelation().getClass()))
								try {rel.getSecondStructure().setProperty(Property.getCASInstance(),value.toString());} catch (Exception x) {};
			}
		} else if ("Core composition".equals(lines[_lines.endpointcategory.ordinal()])) {
			record.setSubstancetype("nanoparticle");
			if ("Element".equals(lines[_lines.endpoint.ordinal()])) {
				//get the protocol to characterize composition from the paper!
				Protocol protocol = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY.getProtocol(lines[_lines.data_gathering_instruments.ordinal()]);
				protocol.addGuideline(lines[_lines.data_gathering_instruments.ordinal()]);

				ReliabilityParams rel = new ReliabilityParams();
				if (lines[_lines.type_of_method.ordinal()].contains("calculation") || lines[_lines.type_of_method.ordinal()].contains("simulation") ) {
					rel.setStudyResultType("estimated by calculation");
				} else {
					rel.setStudyResultType("experimental result");
				}
				ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = getExperiment(I5_ROOT_OBJECTS.SURFACE_CHEMISTRY,record,protocol,rel);
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
		} else if ("Surface modifier".equals(lines[_lines.endpointcategory.ordinal()])) {
			if ("Abbreviated".equals(lines[_lines.endpoint.ordinal()])) {
				//get the protocol to characterize composition from the paper!
				Protocol protocol = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY.getProtocol(lines[_lines.data_gathering_instruments.ordinal()]);
				protocol.addGuideline(lines[_lines.data_gathering_instruments.ordinal()]);
				
				ReliabilityParams rel = new ReliabilityParams();
				if (lines[_lines.type_of_method.ordinal()].contains("calculation") || lines[_lines.type_of_method.ordinal()].contains("simulation") ) {
					rel.setStudyResultType("estimated by calculation");
				} else {
					rel.setStudyResultType("experimental result");
				}
				ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = getExperiment(I5_ROOT_OBJECTS.SURFACE_CHEMISTRY,record,protocol,rel);

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
		} else if ("PROTEOMICS".equals(lines[_lines.endpointcategory.ordinal()])) {
			Integer num = Integer.parseInt(value.toString());
			//if (num>0) {
				I5_ROOT_OBJECTS category = I5_ROOT_OBJECTS.UNKNOWN_TOXICITY; 
				try {
					category = category.valueOf(lines[_lines.endpointcategory.ordinal()]);
				} catch (Exception x) {}
				Protocol protocol = category.getProtocol(lines[_lines.protocol.ordinal()]);
				protocol.addGuideline(lines[_lines.guideline.ordinal()]);
				ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = null;
				ReliabilityParams rel = new ReliabilityParams();
				if (lines[_lines.type_of_method.ordinal()].contains("calculation") || lines[_lines.type_of_method.ordinal()].contains("simulation") ) {
					rel.setStudyResultType("estimated by calculation");
				} else {
					rel.setStudyResultType("experimental result");
				}
				experiment = getExperiment(category,record,protocol,rel);
				if (!"".equals(lines[_lines.type_of_method.ordinal()]))
					experiment.getParameters().put(I5CONSTANTS.cTypeMethod, lines[_lines.type_of_method.ordinal()]);
				if (!"".equals(lines[_lines.type_of_study.ordinal()]))
						experiment.getParameters().put(I5CONSTANTS.cTypeStudy, lines[_lines.type_of_study.ordinal()]);
				if (!"".equals(lines[_lines.data_gathering_instruments.ordinal()]))
					experiment.getParameters().put(I5CONSTANTS.pDATA_GATHERING_INSTRUMENTS, lines[_lines.data_gathering_instruments.ordinal()]);
				
				
				EffectRecord<String,IParams,String> effect = null;
				if (experiment.getEffects()==null || experiment.getEffects().size()==0) { 
					effect = getEffectRecord(category, experiment);
					effect.setEndpoint("Spectral counts");
					effect.setTextValue(new Params());
					experiment.addEffect(effect);
				} else  {
					effect = experiment.getEffects().get(0);
				}
				((Params)effect.getTextValue()).put(lines[_lines.result.ordinal()],new Value<Integer>(num,null));
				//effect.setTextValue(lines[4]);


				
			//}
		} else {
			String line = lines[_lines.result.ordinal()].toLowerCase();
			if (value==null || "".equals(value.toString())) 
				return;
			if ((      "".equals(line) 
					|| "mean".equals(line)
					|| "sd".equals(line)
					|| "std".equals(line)
					|| "sem".equals(line)
					|| "interpretation".equals(line)
					|| "condition".equals(line)
					)
					) {
				I5_ROOT_OBJECTS category = I5_ROOT_OBJECTS.UNKNOWN_TOXICITY; 
				try {
					category = category.valueOf(lines[_lines.endpointcategory.ordinal()]);
				} catch (Exception x) {}
				Protocol protocol = category.getProtocol(lines[_lines.protocol.ordinal()]);
				protocol.addGuideline(lines[_lines.guideline.ordinal()]);
			
				ReliabilityParams rel = new ReliabilityParams();
				if (lines[_lines.type_of_method.ordinal()].contains("calculation") || lines[_lines.type_of_method.ordinal()].contains("simulation") ) {
					rel.setStudyResultType("estimated by calculation");
				} else {
					rel.setStudyResultType("experimental result");
				}
				
				ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = getExperiment(category,record,protocol,rel);
				
				if (!"".equals(lines[_lines.type_of_method.ordinal()]))
					experiment.getParameters().put(I5CONSTANTS.cTypeMethod, lines[_lines.type_of_method.ordinal()]);
				if (!"".equals(lines[_lines.type_of_study.ordinal()]))
						experiment.getParameters().put(I5CONSTANTS.cTypeStudy, lines[_lines.type_of_study.ordinal()]);
				if (!"".equals(lines[_lines.data_gathering_instruments.ordinal()]))
					experiment.getParameters().put(I5CONSTANTS.pDATA_GATHERING_INSTRUMENTS, lines[_lines.data_gathering_instruments.ordinal()]);
				
			
				
				EffectRecord<String,IParams,String> effect = getEffectRecord(category, experiment);
				/* doens't work
				if ("condition".equals(line)) {
					String name = lines[_lines.endpoint.ordinal()];
					try {
						Value cValue = new Value(Double.parseDouble(value.toString()));
						cValue.setUnits(lines[_lines.units.ordinal()]==null?null:lines[_lines.units.ordinal()].trim());
						effect.getConditions().put(name, cValue);
					} catch (Exception x) {
						effect.getConditions().put(name, value.toString());
					}
					experiment.addEffect(effect);
				}
				*/	
				if ("mean".equals(line) || "".equals(line) || "interpretation".equals(line)) {
					String endpoint = lines[_lines.endpoint.ordinal()];
					effect.setEndpoint(endpoint);
					
					for (_lines field : _lines.values()) if (field.isCondition()) {
						if (!"".equals(header.get(0).getValue(field.ordinal()))) {
							String cValue = getConditionValue(lines[field.ordinal()]);
							if (cValue == null || "".equals(cValue)) continue;
								effect.getConditions().put(header.get(0).getValue(field.ordinal()),cValue);
						}
					}	
					
					if (endpoint.toLowerCase().indexOf("mean")<0) effect.setLoQualifier(line);
					if ("interpretation".equals(line)) 
						experiment.setInterpretationResult(value.toString());

					try {
						effect.setLoValue(Double.parseDouble(value.toString()));
						effect.setUnit(lines[_lines.units.ordinal()]==null?null:lines[_lines.units.ordinal()].trim());
					} catch (Exception x) {
						effect.setTextValue(value.toString());
					}
					experiment.addEffect(effect);

				} else if ("sd".equals(line) || "std".equals(line)  || "sem".equals(line)) {
					try {
						effect.setErrQualifier(line);
						effect.setErrorValue(Double.parseDouble(value.toString()));
					} catch (Exception x) {
						effect.setErrorValue((Double)null);
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
				lines[_lines.protocol.ordinal()]+
				lines[_lines.guideline.ordinal()]+
				lines[_lines.type_of_method.ordinal()]+
				lines[_lines.type_of_study.ordinal()]+
				lines[_lines.data_gathering_instruments.ordinal()]+
				lines[_lines.endpoint.ordinal()]+
				lines[_lines.condition1.ordinal()]+
				lines[_lines.condition2.ordinal()]+
				lines[_lines.condition3.ordinal()]
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
	@Override
	protected ProtocolApplication<Protocol, IParams, String, IParams, String> getExperiment(
			I5_ROOT_OBJECTS category, SubstanceRecord record,Protocol protocol,ReliabilityParams reliability) {
		String experimentUUID = prefix+UUID.nameUUIDFromBytes(
				(record.getCompanyUUID() +
						category.name() +
						lines[_lines.protocol.ordinal()]+
						lines[_lines.guideline.ordinal()]+
						lines[_lines.type_of_method.ordinal()]+
						lines[_lines.type_of_study.ordinal()]+
						lines[_lines.data_gathering_instruments.ordinal()]
				                                 //+lines[_lines.endpoint.ordinal()]
				                                        ).getBytes()
				);
		
		if (record.getMeasurements()!=null)
			for (ProtocolApplication<Protocol, IParams, String, IParams, String> experiment : record.getMeasurements()) {
				if (experimentUUID.equals(experiment.getDocumentUUID())) return experiment;
			}
		ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = category.createExperimentRecord(protocol);
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
	protected String getConditionValue(String cell) {
		return cell;
		/*
		String medium = null;
		if ((cell!=null) && cell.indexOf("serum")>0) {
			medium = "Human serum";
		} return medium;
		*/
	}
	
}