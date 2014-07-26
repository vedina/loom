package net.idea.loom.tox21;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.UUID;

import net.idea.i5.io.I5_ROOT_OBJECTS;

import org.openscience.cdk.interfaces.IAtomContainer;

import ambit2.base.data.LiteratureEntry;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.study.ReliabilityParams;
import ambit2.base.data.study.Value;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.IteratingDelimitedFileReader;
import ambit2.core.io.RawIteratingWrapper;

public class Tox21SubstanceReader  extends RawIteratingWrapper<IteratingDelimitedFileReader> {
	private static String prefix = "Tox21";
	
	public Tox21SubstanceReader(IteratingDelimitedFileReader reader) {
		super(reader);
	}
	
	public Tox21SubstanceReader(File file) throws Exception {
		this(new IteratingDelimitedFileReader(new FileReader(file)));
		setReference(new LiteratureEntry(file.getName(),"Tox21"));

	}
		
	
	@Override
	protected IStructureRecord createStructureRecord() {
		return new SubstanceRecord();
	}

	private enum _field {
		PUBCHEM_SID {
			@Override
			public void parse(String key,EffectRecord<String,IParams,String> effect,SubstanceRecord r, IAtomContainer mol) {
				try {
					r.setCompanyUUID(mol.getProperty(name()).toString());
					r.setPublicName(name() + " " + mol.getProperty(name()).toString());
					r.getMeasurements().get(0).setReferenceSubstanceUUID(r.getCompanyUUID());
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		},
		PUBCHEM_CID {
			@Override
			public void parse(String key,EffectRecord<String,IParams,String> effect,SubstanceRecord r, IAtomContainer mol) {
				try {
					r.setCompanyName(name() + " " + mol.getProperty(name()).toString());
				} catch (Exception x) {
					x.printStackTrace();
				}
			}			
		},
		PUBCHEM_ACTIVITY_OUTCOME {
			@Override
			public void parse(String key,EffectRecord<String,IParams,String> effect,SubstanceRecord r, IAtomContainer mol) {
				r.getMeasurements().get(0).setInterpretationResult(mol.getProperty(name()).toString());
			}
		},
		PUBCHEM_ACTIVITY_SCORE {
			@Override
			public void parse(String key,EffectRecord<String,IParams,String> effect,SubstanceRecord r, IAtomContainer mol) {
				r.getMeasurements().get(0).setInterpretationCriteria(mol.getProperty(name()).toString());
			}			
		},
		PUBCHEM_ACTIVITY_URL {
			@Override
			public void parse(String key,EffectRecord<String,IParams,String> effect,SubstanceRecord r, IAtomContainer mol) {
				r.getMeasurements().get(0).setReferenceYear("2014");
				r.getMeasurements().get(0).setReferenceOwner("Tox21");
				r.getMeasurements().get(0).setReference(mol.getProperty(name()).toString());
			}
		},
		PUBCHEM_ASSAYDATA_COMMENT,
		ACTIVITY {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
			}
		},
		Phenotype {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}
		},
		Potency {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}
		},
		Efficacy {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}
		},
		Analysis_Comment {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}					
		},
		Activity_Score {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}			
		},
		Curve_Description {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}					
		},
		Fit_LogAC50 {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}			
		},
		Fit_HillSlope {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}				
		},
		Fit_R2 {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}				
		},
		Fit_InfiniteActivity {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}				
		},
		Fit_ZeroActivity {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}					
		},
		Fit_CurveClass {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}
		},
		Excluded_Points {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}			
		},
		Max_Response {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}	
		},
		Compound_QC {
			//TODO
		},
		Sample_Source {
			
		},
		ATAD5_Activity {
			
		},
		ATAD5_Potency__uM_ {
			
		},
		ATAD5_Efficacy____ {
			
		},
		AhR_Activity {
			
		},
		AhR_Potency__uM_ {
			
		},
		AhR_Efficacy____ {
			
		},		
		ER_Activity {
			
		},
		ER_Potency__uM_ {
			
		},
		ER_Efficacy____ {
			
		},	
		R_Activity {
			
		},
		R_Potency__uM_ {
			
		},
		R_Efficacy____ {
			
		},			
		Viability_Activity {
			
		},
		
		Viability_Efficacy____ {
			
		},
		Viability_Potency__uM_ {
			
		},
		Antagonist_Activity {
			
		},
		Antagonist_Efficacy____ {
			
		},
		Antagonist_Potency__uM_ {
			
		},		
		Activity_Summary {
			
		}

		;
		public void parse(String key,EffectRecord<String,IParams,String> effect,SubstanceRecord r,IAtomContainer mol) {
			
		}
	}

	protected ProtocolApplication<Protocol, IParams, String, IParams, String> getExperiment(
			I5_ROOT_OBJECTS category, SubstanceRecord record,Protocol protocol,ReliabilityParams reliability) {
		String experimentUUID = prefix+UUID.nameUUIDFromBytes(
				(record.getCompanyUUID() +
						category.name() +
						protocol.getGuideline().get(0)
						/*
						lines[_lines.protocol.ordinal()]+
						lines[_lines.guideline.ordinal()]+
						lines[_lines.type_of_method.ordinal()]+
						lines[_lines.type_of_study.ordinal()]+
						lines[_lines.data_gathering_instruments.ordinal()]
				                                 //+lines[_lines.endpoint.ordinal()]
				                                  * */
				                                        ).getBytes()
				);
		
		if (record.getMeasurements()!=null)
			for (ProtocolApplication<Protocol, IParams, String, IParams, String> experiment : record.getMeasurements()) {
				if (experimentUUID.equals(experiment.getDocumentUUID())) return experiment;
			}
		ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = category.createExperimentRecord(protocol);
		experiment.setReliability(reliability);
		experiment.setParameters(new Params());
		//setCitation(experiment);
		experiment.setDocumentUUID(experimentUUID);
		record.addMeasurement(experiment);
		return experiment;
	}
	
	@Override
	protected Object transform(Object o) {
		if (o instanceof IAtomContainer) try {

			r.clear();
			r.setFormat("SDF");
			java.util.Set<Object> keys = ((IAtomContainer)o).getProperties().keySet();
			
			
			Protocol protocol = I5_ROOT_OBJECTS.UNKNOWN_TOXICITY.getProtocol(getReference().getName());
			protocol.addGuideline(getReference().getName());

			ReliabilityParams rel = new ReliabilityParams();
			rel.setStudyResultType("experimental result");
			
			
			ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = 
								getExperiment(I5_ROOT_OBJECTS.UNKNOWN_TOXICITY,(SubstanceRecord)r,protocol,rel);			
			Iterator  i = keys.iterator();
			EffectRecord<String,IParams,String> effect;
			while (i.hasNext()) {
				effect = null;
				Object key = i.next();
				String thetag = key.toString();
				try {
					Object value = ((IAtomContainer)o).getProperty(key.toString());
					if (value==null || "".equals(value.toString().trim())) continue;
					effect = I5_ROOT_OBJECTS.UNKNOWN_TOXICITY.createEffectRecord();
					experiment.addEffect(effect);
					
					_field field = null;
					
					int ix = thetag.indexOf("W460-");
					if (ix==0) {
						thetag = thetag.substring(5);
						Value v = new Value(460);
						v.setUnits("nm");
						effect.getConditions().put("Emission wavelength", v);
					} else {
						ix = thetag.indexOf("W530-");
						if (ix==0) {
							thetag = thetag.substring(5);
							Value v = new Value(530);
							v.setUnits("nm");
							effect.getConditions().put("Emission wavelength", v);
						} else {
							ix = thetag.indexOf("Ratio-");
							if (ix==0) {
								thetag = thetag.substring(6);
								Value v = new Value("460nm/530nm");
								effect.getConditions().put("Ratio", v);
							}	
						}
					}
					int replicate_index = thetag.indexOf("-Replicate");
					if (replicate_index>0) {
						effect.getConditions().put("Replicate", new Value(
								Integer.parseInt(thetag.substring(replicate_index).replace("-Replicate_", ""))));
						String tag = thetag.substring(0,replicate_index);
						int activity_index  = tag.indexOf("Activity at");
						if (activity_index>=0) {
							field= _field.ACTIVITY;
							String concentration = tag.substring(activity_index+12);
							String[] c = concentration.split(" ");
							Value val = new Value(Double.parseDouble(c[0]));
							val.setUnits(c[1]);
							effect.getConditions().put("Concentration", val);
							try {
								effect.setLoValue(Double.parseDouble(((IAtomContainer)o).getProperty(key.toString()).toString()));
							} catch (Exception x) {
								effect.setTextValue(((IAtomContainer)o).getProperty(key.toString()).toString());
							}
							continue;
						} else 
							field =  _field.valueOf(tag.replace(" ", "_").replace("-","_").replace("%","_").replace("(","_").replace(")","_"));
					} else 
						field =  _field.valueOf(thetag.replace(" ", "_").replace("-","_").replace("%","_").replace("(","_").replace(")","_"));
					
					field.parse(key.toString(),effect,(SubstanceRecord)r, (IAtomContainer)o);
				} catch (Exception x) {
					System.err.println(key);
					x.printStackTrace();	
				}
				
//				r.setProperty(key, ((IAtomContainer)o).getProperties().get(key));
			}
			
			/*
			if (((SubstanceRecord)r).getCompanyUUID()==null)
				((SubstanceRecord)r).setCompanyUUID(reader.getPrefix()+UUID.randomUUID());
			
			//owner is the dataset
			((SubstanceRecord)r).setOwnerName(reader.getReference().getName());
			((SubstanceRecord)r).setOwnerUUID(reader.getPrefix() + UUID.nameUUIDFromBytes(reader.getReference().getURL().toString().getBytes()));
			//ids.add(new ExternalIdentifier("DOI","http://dx.doi.org/10.1021/nn406018q"));
			
			*/
			((IAtomContainer)o).getProperties().clear();


			r.setContent(writer.process((IAtomContainer)o));
			Object ref = ((IAtomContainer)o).getProperty("REFERENCE");
			if (ref instanceof LiteratureEntry)
				r.setReference((LiteratureEntry)ref);
			else r.setReference(getReference());
			
			return r;  
		} catch (Exception x) {
			r.clear();
			r.setFormat("SDF");
			r.setContent(null);
			r.setReference(getReference());
			return r;  
		} else return o;
	}
}