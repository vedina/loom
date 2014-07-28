package net.idea.loom.tox21;

import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
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
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.IteratingDelimitedFileReader;
import ambit2.core.io.RawIteratingWrapper;

public class Tox21SubstanceReader  extends RawIteratingWrapper<IteratingDelimitedFileReader> {
	private static String prefix = "TX21-";
	
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
			public void parse(String key,EffectRecord<String,IParams,String> effect,ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				try {
					String value = mol.getProperty(name()).toString();
					String uuid;
					try {
						uuid = "PSID-"+UUID.nameUUIDFromBytes(BigInteger.valueOf(Long.getLong(value)).toByteArray());
					} catch (Exception x) {
						uuid = "PSID-"+UUID.nameUUIDFromBytes(value.getBytes());	
					}
					if (r.getExternalids()==null) r.setExternalids(new ArrayList<ExternalIdentifier>());
					r.getExternalids().add(new ExternalIdentifier(name(),value));
					
					experiment0.setSubstanceUUID(uuid);
					experiment1.setSubstanceUUID(uuid);
					r.setCompanyName(name() + " " + value);
					r.setCompanyUUID(uuid);
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		},
		PUBCHEM_CID {
			@Override
			public void parse(String key,EffectRecord<String,IParams,String> effect,ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				try {
					String value = mol.getProperty(name()).toString();
					String uuid;
					try {
						uuid = "PCID-"+UUID.nameUUIDFromBytes(BigInteger.valueOf(Long.getLong(value)).toByteArray());
					} catch (Exception x) {
						uuid = "PCID-"+UUID.nameUUIDFromBytes(value.getBytes());	
					}
					if (r.getExternalids()==null) r.setExternalids(new ArrayList<ExternalIdentifier>());
					r.getExternalids().add(new ExternalIdentifier(name(),value));
					r.setPublicName(name() + " " + value);
					r.setReferenceSubstanceUUID(uuid);
					experiment0.setReferenceSubstanceUUID(uuid);
					experiment1.setReferenceSubstanceUUID(uuid);
				} catch (Exception x) {
					x.printStackTrace();
				}
			}			
		},
		PUBCHEM_ACTIVITY_OUTCOME {
			@Override
			public void parse(String key,EffectRecord<String,IParams,String> effect,ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				experiment0.setInterpretationResult(mol.getProperty(name()).toString());
				experiment1.setInterpretationResult(mol.getProperty(name()).toString());
			}
			@Override
			public String toString() {
				return "The BioAssay activity outcome";
			}
			@Override
			public int getLevel() {
				return 0;
			}
		},
		PUBCHEM_ACTIVITY_SCORE {
			@Override
			public void parse(String key,EffectRecord<String,IParams,String> effect,ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				experiment0.setInterpretationCriteria(mol.getProperty(name()).toString());
				experiment1.setInterpretationCriteria(mol.getProperty(name()).toString());
			}			
			@Override
			public String toString() {
				return "The BioAssay activity ranking score";
			}
			@Override
			public int getLevel() {
				return 0;
			}
		},
		PUBCHEM_ACTIVITY_URL {
			@Override
			public void parse(String key,EffectRecord<String,IParams,String> effect,ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				String value = mol.getProperty(key).toString();
				if (r.getExternalids()==null) r.setExternalids(new ArrayList<ExternalIdentifier>());
				r.getExternalids().add(new ExternalIdentifier(name(),value));		
			}
			
		},
		PUBCHEM_ASSAYDATA_COMMENT {
			@Override
			public void parse(
					String key,
					EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				String value = mol.getProperty(key).toString();
			}

		},
		Activity {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				try {
					effect.setLoValue(Double.parseDouble(mol.getProperty(key.toString()).toString()));
				} catch (Exception x) {
					effect.setTextValue(mol.getProperty(key.toString()).toString());
				}				
			}
			@Override
			public int getLevel() {
				return 0;
			}
			@Override
			public String toString() {
				return "% Activity at given concentration.";
			}
		},
		Phenotype {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}
			@Override
			public int getLevel() {
				return 0;
			}
			@Override
			public String toString() {
				return "Indicates type of activity observed: inhibitor, activator, fluorescent, cytotoxic, inactive, or inconclusive.";
			}
		},
		Potency {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}
			@Override
			public int getLevel() {
				return 0;
			}
			@Override
			public String toString() {
				return "Concentration at which compound exhibits half-maximal efficacy, AC50. Extrapolated AC50s also include the highest efficacy observed and the concentration of compound at which it was observed.";
			}
			
		},
		Efficacy {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}
			@Override
			public int getLevel() {
				return 0;
			}
			@Override
			public String toString() {
				return "Maximal efficacy of compound, reported as a percentage of control. These values are estimated based on fits of the Hill equation to the dose-response curves.";
			}
		},
		Analysis_Comment {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}
			@Override
			public String toString() {
				return "Annotation/notes on a particular compound's data or its analysis.";
			}
		},
		Activity_Score {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}			
			@Override
			public String toString() {
				return "Activity score";
			}
		},
		Curve_Description {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}
			@Override
			public String toString() {
				return "A description of dose-response curve quality. A complete curve has two observed asymptotes; a partial curve may not have attained its second asymptote at the highest concentration tested. High efficacy curves exhibit efficacy greater than 80% of control. Partial efficacies are statistically significant, but below 80% of control.";
			}
		},
		Fit_LogAC50 {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}			
			@Override
			public String toString() {
				return "The logarithm of the AC50 from a fit of the data to the Hill equation (calculated based on Molar Units).";
			}
		},
		Fit_HillSlope {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}				
		},
		Fit_R2 {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}				
			@Override
			public String toString() {
				return "R^2 fit value of the curve. Closer to 1.0 equates to better Hill equation fit.";
			}
		},
		Fit_InfiniteActivity {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}				
			@Override
			public String toString() {
				return "The asymptotic efficacy from a fit of the data to the Hill equation.";
			}
		},
		Fit_ZeroActivity {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}					
			@Override
			public String toString() {
				return "Efficacy at zero concentration of compound from a fit of the data to the Hill equation.";
			}
		},
		Fit_CurveClass {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}
			@Override
			public String toString() {
				return "Numerical encoding of curve description for the fitted Hill equation.";
			}
		},
		Excluded_Points {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}
			@Override
			public String toString() {
				return "Which dose-response titration points were excluded from analysis based on outlier analysis. Each number represents whether a titration point was (1) or was not (0) excluded, for the titration series going from smallest to highest compound concentrations.";
			}
		},
		Max_Response {
			@Override
			public void parse(String key,EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
			}	
			@Override
			public String toString() {
				return "Maximum activity observed for compound (usually at highest concentration tested).";
			}
		},
		Compound_QC {
			@Override
			public void parse(String key,
					EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				String value = mol.getProperty(key).toString().replace("QC'd by ", "").replace("\"", "");
				String uuid = prefix+UUID.nameUUIDFromBytes(value.getBytes());
				r.setOwnerName(value);
				r.setOwnerUUID(uuid);
				experiment0.setCompanyName(value);
				experiment0.setCompanyUUID(uuid);
				experiment1.setCompanyName(value);
				experiment1.setCompanyUUID(uuid);				
			}			
		},
		Compound_QC_Replicate_1 {
			@Override
			public void parse(String key,
					EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				String value = mol.getProperty(key).toString().replace("QC'd by ", "").replace("\"", "");
				String uuid = prefix+UUID.nameUUIDFromBytes(value.getBytes());
				r.setOwnerName(value);
				r.setOwnerUUID(uuid);
				experiment0.setCompanyName(value);
				experiment0.setCompanyUUID(uuid);
				experiment1.setCompanyName(value);
				experiment1.setCompanyUUID(uuid);
			}			
		},
		Sample_Source {
			@Override
			public void parse(String key,
					EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				String value = mol.getProperty(key).toString();
				String uuid = prefix+UUID.nameUUIDFromBytes(value.getBytes());
				r.setOwnerName(value);
				r.setOwnerUUID(uuid);
				experiment0.setCompanyName(value);
				experiment0.setCompanyUUID(uuid);
				experiment1.setCompanyName(value);
				experiment1.setCompanyUUID(uuid);
			}
			@Override
			public String toString() {
				return "Where sample was obtained";
			}
		},
		Activity_Summary {
			@Override
			public void parse(String key,
					EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}
		},
		Auto_fluorescence_outcome {
			@Override
			public void parse(String key,
					EffectRecord<String, IParams, String> effect,
					ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
				effect.setEndpoint(name());
				effect.setTextValue(mol.getProperty(key).toString());
			}
			@Override
			public int getLevel() {
				return 0;
			}
			@Override
			public String toString() {
				return "Type of compound activity in the auto fluorescence counter screens.";
			}
		}
		
		;
		public void parse(String key,EffectRecord<String,IParams,String> effect,ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1, SubstanceRecord r, IAtomContainer mol) {
			
		}
		public int getLevel() {
			return 1;
		}
	}

	protected ProtocolApplication<Protocol, IParams, String, IParams, String> getExperiment(
			I5_ROOT_OBJECTS category,IAtomContainer mol, 
			SubstanceRecord record,Protocol protocol,ReliabilityParams reliability,int level) {
		
		String experimentUUID = prefix+UUID.nameUUIDFromBytes(
				(record.getCompanyUUID() +
						category.name() +
						getReference().getTitle() +
						Integer.toString(level) + 						
						mol.getProperty(_field.PUBCHEM_SID)
						).getBytes()
				);
		
		if (record.getMeasurements()!=null)
			for (ProtocolApplication<Protocol, IParams, String, IParams, String> experiment : record.getMeasurements()) {
				if (experimentUUID.equals(experiment.getDocumentUUID())) return experiment;
			}
		ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = category.createExperimentRecord(protocol);
		experiment.setReliability(reliability);
		experiment.setParameters(new Params());
		experiment.setReferenceYear("2014");
		experiment.setReferenceOwner(getReference().getName());
		
		//setCitation(experiment);
		experiment.setDocumentUUID(experimentUUID);
		record.addMeasurement(experiment);
		return experiment;
	}
	
	protected String parseTag(String thetag,EffectRecord<String,IParams,String> effect) {
		int ix = thetag.indexOf("W460-");
		if (ix==0) {
			Value v = new Value(460);
			v.setUnits("nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(5);
		}
		ix = thetag.indexOf("W530-");
		if (ix==0) {
			Value v = new Value(530);
			v.setUnits("nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(5);
		}
		ix = thetag.indexOf("W535-");
		if (ix==0) {
			Value v = new Value(535);
			v.setUnits("nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(5);
		}
		ix = thetag.indexOf("W590-");
		if (ix==0) {
			Value v = new Value(590);
			v.setUnits("nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(5);
		}		
		ix = thetag.indexOf("530 nm ");
		if (ix==0) {
			Value v = new Value(530);
			v.setUnits("nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(7);
		}
		ix = thetag.indexOf("535 nm ");
		if (ix==0) {
			Value v = new Value(535);
			v.setUnits("nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(7);
		}		
		ix = thetag.indexOf("590 nm ");
		if (ix==0) {
			Value v = new Value(590);
			v.setUnits("nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(7);
		}			
		ix = thetag.indexOf("460 nm ");
		if (ix==0) {
			Value v = new Value(460);
			v.setUnits("nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(7);
		}
		
		ix = thetag.indexOf("Ratio-");
		if (ix==0) {
			Value v = new Value("Ratio 460nm/530nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(6);
		}	
		ix = thetag.indexOf("Ratio");
		if (ix==0) {
			Value v = new Value("Ratio 460nm/530nm");
			effect.getConditions().put("Emission wavelength", v);
			return thetag.substring(6);
		}		
		
		ix = thetag.indexOf("Blue (460 nm) auto fluorescence outcome");
		if (ix==0) {
			Value v = new Value(460);
			v.setUnits("nm");
			effect.getConditions().put("Emission wavelength", v);
			return "Auto fluorescence outcome";
		}
		 
		ix = thetag.indexOf("AhR ");
		if (ix==0) {
			Value v = new Value("Aryl hydrocarbon receptor");
			effect.getConditions().put("Target", v);
			//http://www.ncbi.nlm.nih.gov/protein/51095037
			return thetag.substring(4);
		}		
		
		ix = thetag.indexOf("TR ");
		if (ix==0) {
			Value v = new Value("Thyroid receptor");
			effect.getConditions().put("Target", v);
			return thetag.substring(3);
		}		
		
		ix = thetag.indexOf("AR ");
		if (ix==0) {
			Value v = new Value("Androgen receptor");
			effect.getConditions().put("Target", v);
			//http://www.ncbi.nlm.nih.gov/protein/124375976
			return thetag.substring(3);
		}
		
		ix = thetag.indexOf("ER ");
		if (ix==0) {
			Value v = new Value("Estrogen receptor alpha");
			effect.getConditions().put("Target", v);
			//http://www.ncbi.nlm.nih.gov/protein/348019627
			return thetag.substring(3);
		}
		
		ix = thetag.indexOf("ATAD5 ");
		if (ix==0) {
			Value v = new Value("ATAD5");
			effect.getConditions().put("Target", v);
			//http://www.ncbi.nlm.nih.gov/protein/296439460
			return thetag.substring(6);
		}		
		

		
		ix = thetag.indexOf("Antagonist ");
		if (ix==0) {
			Value v = new Value("Aromatase inhibitor");
			effect.getConditions().put("Target", v);
			//http://www.ncbi.nlm.nih.gov/protein/119597822
			return thetag.substring(10);
		}		
		
		ix = thetag.indexOf("Viability");
		if (ix==0) {
			Value v = new Value("Viability");
			effect.getConditions().put("Measurand", v);
			return thetag.substring(9);
		}
		return thetag;
	}
	protected String parseReplicate(String thetag,EffectRecord<String,IParams,String> effect) {
		int replicate_index = thetag.indexOf("-Replicate");
		if (replicate_index>0) {
			Value value = new Value(
					Integer.parseInt(thetag.substring(replicate_index).replace("-Replicate_", "")));
			value.setLoQualifier("=");

			effect.getConditions().put("Replicate", value);
			return thetag.substring(0,replicate_index);
		}
		return thetag;
	}	
	protected String parseActivity(String thetag,EffectRecord<String,IParams,String> effect) {
		
		int ix  = thetag.indexOf("Activity at");
		if (ix>=0) {
			String concentration = thetag.substring(ix+12);
			String[] c = concentration.split(" ");
			Value val = new Value(Double.parseDouble(c[0]));
			val.setUnits(c[1]);
			effect.getConditions().put("Concentration", val);
			return "Activity";
		}
		ix  = thetag.indexOf("(%)");
		if (ix>=0) {
			effect.setUnit("%");
			return thetag.substring(0,ix-1).trim();
		}
		ix  = thetag.indexOf("(uM)");
		if (ix>=0) {
			effect.setUnit("uM");
			return thetag.substring(0,ix-1).trim();
		}
		return thetag;
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
			
			
			
			ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0 =
				getExperiment(I5_ROOT_OBJECTS.UNKNOWN_TOXICITY,(IAtomContainer)o,(SubstanceRecord)r,protocol,rel,0);
			
			ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1 =			
				getExperiment(I5_ROOT_OBJECTS.UNKNOWN_TOXICITY,(IAtomContainer)o,(SubstanceRecord)r,protocol,rel,1);								
			
			Iterator  i = keys.iterator();
			EffectRecord<String,IParams,String> effect;
			while (i.hasNext()) {
				effect = null;
				Object key = i.next();
				String thetag = key.toString();
				try {
					Object value = ((IAtomContainer)o).getProperty(key.toString());
					if (value==null || "".equals(value.toString().trim())) continue;
					
					try {
						_field field =  _field.valueOf(thetag.trim().replace(" ", "_").replace("-","_").replace("%","_").replace("(","_").replace(")","_"));
						field.parse(key.toString(),effect,experiment0,experiment1,(SubstanceRecord)r, (IAtomContainer)o);
						continue;
					} catch (Exception x) {
						//further processing
					}
							
							
					effect = I5_ROOT_OBJECTS.UNKNOWN_TOXICITY.createEffectRecord();
					
					_field field = null;
					
					thetag = parseReplicate(thetag, effect);
					thetag = parseTag(thetag, effect);
					
					thetag = parseActivity(thetag, effect);
					if (thetag==null) continue;
					/*
					int activity_index  = thetag.indexOf("Activity at");
					if (activity_index>=0) {
						field= _field.Activity;
						String concentration = thetag.substring(activity_index+12);
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
						field =  _field.valueOf(thetag.replace(" ", "_").replace("-","_").replace("%","_").replace("(","_").replace(")","_"));
					*/	
					field =  _field.valueOf(thetag.trim().replace(" ", "_").replace("-","_").replace("%","_").replace("(","_").replace(")","_"));
					
					field.parse(key.toString(),effect,experiment0,experiment1,(SubstanceRecord)r, (IAtomContainer)o);
					if (field.getLevel()==0)
						experiment0.addEffect(effect);
					else experiment1.addEffect(effect);
					
					if (effect.getEndpoint()==null) {
						throw new Exception("Null effect");
					}
				} catch (Exception x) {
					System.err.println(key);
					System.err.println(x.getMessage());
					//x.printStackTrace();	
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