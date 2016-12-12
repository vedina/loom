package net.idea.loom.pubchem.rest;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import ambit2.core.data.MoleculeTools;
import ambit2.core.io.IteratingDelimitedFileReader;
import ambit2.core.io.RawIteratingWrapper;
import net.idea.i5.io.I5CONSTANTS;
import net.idea.i5.io.I5_ROOT_OBJECTS;

/**
 * 
 * @author nina
 * 
 */

public class PubChemAIDReader extends RawIteratingWrapper<IteratingDelimitedFileReader> {
    private static String prefix = "PCHM-";
    protected PubChemAIDMetadata metadata;

    protected boolean readPubchemScoreOnly = false;

    public boolean isReadPubchemScoreOnly() {
	return readPubchemScoreOnly;
    }

    public void setReadPubchemScoreOnly(boolean readPubchemScoreOnly) {
	this.readPubchemScoreOnly = readPubchemScoreOnly;
    }

    public PubChemAIDReader(IteratingDelimitedFileReader reader) {
	super(reader);
    }

    public PubChemAIDReader(File file, InputStream json_meta) throws Exception {
	this(file, json_meta, null);
    }

    public PubChemAIDReader(File file, InputStream json_meta, String[] activity_outcome_method) throws Exception {
	this(new IteratingDelimitedFileReader(new FileReader(file)));
	metadata = initMetadata(json_meta);
	if ((activity_outcome_method != null)
		&& (Arrays.binarySearch(activity_outcome_method, metadata.getActivityOutcomeMethod()) < 0))
	    throw new Exception(metadata.getActivityOutcomeMethod());

	setReference(new LiteratureEntry(String.format("ID%s (%s)", metadata.getAIDSource_name(),
		metadata.getAIDSource_id()), metadata.getDescriptionAsText()));
	try {
	    json_meta.close();
	} catch (Exception x) {
	}
    }

    protected PubChemAIDMetadata initMetadata(InputStream in) throws Exception {
	ObjectMapper om = new ObjectMapper();
	try {
	    JsonNode node = om.readTree(new InputStreamReader(in)).get("PC_AssayContainer");
	    ((ObjectNode) node.get(0).get("assay")).put("tags", om.createObjectNode());
	    return new PubChemAIDMetadata(node.get(0));
	} catch (Exception x) {
	    throw x;
	} finally {
	    try {
		in.close();
	    } catch (Exception x) {
	    }
	}
    }

    @Override
    protected IStructureRecord createStructureRecord() {
	return new SubstanceRecord();
    }

    private enum _field_top {
	PUBCHEM_SID {
	    @Override
	    public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		    SubstanceRecord r, IAtomContainer mol) {
		try {
		    String value = mol.getProperty(name()).toString();
		    String uuid;
		    try {
			uuid = "PSID-"
				+ UUID.nameUUIDFromBytes(BigInteger.valueOf(Long.parseLong(value)).toByteArray());
		    } catch (Exception x) {
			uuid = "PSID-" + UUID.nameUUIDFromBytes(value.getBytes());
		    }
		    if (r.getExternalids() == null)
			r.setExternalids(new ArrayList<ExternalIdentifier>());
		    r.getExternalids().add(new ExternalIdentifier(name(), value));

		    experiment0.setSubstanceUUID(uuid);
		    r.setSubstanceName(name() + " " + value);
		    r.setSubstanceUUID(uuid);
		} catch (Exception x) {
		    x.printStackTrace();
		}
	    }
	},
	PUBCHEM_CID {
	    @Override
	    public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		    SubstanceRecord r, IAtomContainer mol) {
		try {
		    String value = mol.getProperty(name()).toString();
		    String uuid;
		    try {
			uuid = "PCID-"
				+ UUID.nameUUIDFromBytes(BigInteger.valueOf(Long.parseLong(value)).toByteArray());
		    } catch (Exception x) {
			x.printStackTrace();
			uuid = "PCID-" + UUID.nameUUIDFromBytes(value.getBytes());
		    }
		    if (r.getExternalids() == null)
			r.setExternalids(new ArrayList<ExternalIdentifier>());
		    r.getExternalids().add(new ExternalIdentifier(name(), value));
		    r.setPublicName(name() + " " + value);
		    r.setReferenceSubstanceUUID(uuid);
		    experiment0.setReferenceSubstanceUUID(uuid);

		} catch (Exception x) {
		    x.printStackTrace();
		}
	    }
	},
	PUBCHEM_ACTIVITY_OUTCOME {
	    @Override
	    public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		    SubstanceRecord r, IAtomContainer mol) {
		experiment0.setInterpretationResult(mol.getProperty(name()).toString());

	    }

	    @Override
	    public String toString() {
		return "The BioAssay activity outcome";
	    }

	},
	PUBCHEM_ACTIVITY_SCORE {
	    @Override
	    public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		    SubstanceRecord r, IAtomContainer mol) {
		// experiment0.setInterpretationCriteria(name().replace("_",
		// " ")+ "=" + mol.getProperty(name()).toString());
	    }

	    @Override
	    public String toString() {
		return "The BioAssay activity ranking score";
	    }

	    @Override
	    public void addEffectRecord(String key,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, EffectRecord effect,
		    SubstanceRecord r, IAtomContainer mol) {
		effect.setEndpoint(name().replace("_", " "));
		experiment0.addEffect(effect);
		try {
		    effect.setLoValue(Double.parseDouble(mol.getProperty(name()).toString()));
		} catch (Exception x) {
		    effect.setTextValue(mol.getProperty(name()).toString());
		}
	    }
	},
	PUBCHEM_ACTIVITY_URL {
	    @Override
	    public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		    SubstanceRecord r, IAtomContainer mol) {
		String value = mol.getProperty(key).toString();
		if (r.getExternalids() == null)
		    r.setExternalids(new ArrayList<ExternalIdentifier>());
		r.getExternalids().add(new ExternalIdentifier(name(), value));
	    }

	},
	PUBCHEM_ASSAYDATA_COMMENT {
	    @Override
	    public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		    SubstanceRecord r, IAtomContainer mol) {
		String value = mol.getProperty(key).toString();
	    }

	},
	Compound_QC {
	    @Override
	    public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		    SubstanceRecord r, IAtomContainer mol) {
		String value = mol.getProperty(key).toString().replace("QC'd by ", "").replace("\"", "");
		String uuid = prefix + UUID.nameUUIDFromBytes(value.getBytes());
		r.setOwnerName(value);
		r.setOwnerUUID(uuid);
		experiment0.setCompanyName(value);
		experiment0.setCompanyUUID(uuid);

	    }
	},
	Sample_Source {
	    @Override
	    public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		    SubstanceRecord r, IAtomContainer mol) {
		String value = mol.getProperty(key).toString();
		String uuid = prefix + UUID.nameUUIDFromBytes(value.getBytes());
		r.setOwnerName(value);
		r.setOwnerUUID(uuid);
		experiment0.setCompanyName(value);
		experiment0.setCompanyUUID(uuid);
	    }

	    @Override
	    public String toString() {
		return "Where sample was obtained";
	    }
	},
	Compound_QC_Replicate_1 {
	    @Override
	    public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		    SubstanceRecord r, IAtomContainer mol) {
		String value = mol.getProperty(key).toString().replace("QC'd by ", "").replace("\"", "");
		String uuid = prefix + UUID.nameUUIDFromBytes(value.getBytes());
		r.setOwnerName(value);
		r.setOwnerUUID(uuid);
		experiment0.setCompanyName(value);
		experiment0.setCompanyUUID(uuid);
	    }
	};
	public void parse(String key, ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0,
		SubstanceRecord r, IAtomContainer mol) {

	}

	public void addEffectRecord(String key,
		ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, EffectRecord effect,
		SubstanceRecord r, IAtomContainer mol) {

	}
    };

    private enum _field {
	Activity {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    // AC50 http://www.bioassayontology.org/bao#BAO_0000186

	},
	Efficacy {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	Viability_Activity {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
		return "";
	    }
	},
	Viability_Potency {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public int getLevel() {
		return 0;
	    }

	    @Override
	    public String toString() {
		return "";
	    }

	},
	Viability_Efficacy {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public int getLevel() {
		return 0;
	    }

	    @Override
	    public String toString() {
		return "";
	    }
	},
	Ratio_Activity_Score {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
		return "";
	    }
	},
	Ratio_Activity {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
		return "";
	    }
	},
	Ratio_Potency {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public int getLevel() {
		return 0;
	    }

	    @Override
	    public String toString() {
		return "";
	    }

	},
	Ratio_Efficacy {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public int getLevel() {
		return 0;
	    }

	    @Override
	    public String toString() {
		return "";
	    }
	},
	Analysis_Comment {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public String toString() {
		return "Activity score";
	    }

	    @Override
	    public int getLevel() {
		return 0;
	    }
	},
	Curve_Description {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setTextValue(mol.getProperty(key).toString());
	    }

	    @Override
	    public String toString() {
		return "A description of dose-response curve quality. A complete curve has two observed asymptotes; a partial curve may not have attained its second asymptote at the highest concentration tested. High efficacy curves exhibit efficacy greater than 80% of control. Partial efficacies are statistically significant, but below 80% of control.";
	    }
	},
	Ratio_Curve_Description {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public String toString() {
		return "The logarithm of the AC50 from a fit of the data to the Hill equation (calculated based on Molar Units).";
	    }
	},
	Ratio_Fit_LogAC50 {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public String toString() {
		return "The logarithm of the AC50 from a fit of the data to the Hill equation (calculated based on Molar Units).";
	    }
	},
	Ratio_Fit_HillSlope {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }
	},
	Fit_HillSlope {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }
	},
	Fit_R2 {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public String toString() {
		return "R^2 fit value of the curve. Closer to 1.0 equates to better Hill equation fit.";
	    }
	},
	Ratio_Fit_R2 {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public String toString() {
		return "The asymptotic efficacy from a fit of the data to the Hill equation.";
	    }
	},
	Ratio_Fit_InfiniteActivity {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public String toString() {
		return "Efficacy at zero concentration of compound from a fit of the data to the Hill equation.";
	    }
	},
	Ratio_Fit_ZeroActivity {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public String toString() {
		return "Numerical encoding of curve description for the fitted Hill equation.";
	    }
	},
	Ratio_Fit_CurveClass {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setTextValue(mol.getProperty(key).toString());
	    }

	    @Override
	    public String toString() {
		return "Which dose-response titration points were excluded from analysis based on outlier analysis. Each number represents whether a titration point was (1) or was not (0) excluded, for the titration series going from smallest to highest compound concentrations.";
	    }
	},
	Ratio_Excluded_Points {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setLoValue(Double.parseDouble(mol.getProperty(key).toString()));
	    }

	    @Override
	    public String toString() {
		return "Maximum activity observed for compound (usually at highest concentration tested).";
	    }
	},
	Ratio_Max_Response {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		String value = mol.getProperty(key).toString().replace("QC'd by ", "").replace("\"", "");
		String uuid = prefix + UUID.nameUUIDFromBytes(value.getBytes());
		r.setOwnerName(value);
		r.setOwnerUUID(uuid);
		experiment0.setCompanyName(value);
		experiment0.setCompanyUUID(uuid);
	    }
	},
	Activity_Summary {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
		effect.setEndpoint(name());
		effect.setTextValue(mol.getProperty(key).toString());
	    }
	},
	Auto_fluorescence_outcome {
	    @Override
	    public void parse(String key, EffectRecord<String, IParams, String> effect,
		    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		    IAtomContainer mol) {
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
	public void parse(String key, EffectRecord<String, IParams, String> effect,
		ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0, SubstanceRecord r,
		IAtomContainer mol) {

	}

	public int getLevel() {
	    return 1;
	}
    }

    protected ProtocolApplication<Protocol, IParams, String, IParams, String> getExperiment(I5_ROOT_OBJECTS category,
	    IAtomContainer mol, SubstanceRecord record, Protocol protocol, ReliabilityParams reliability, int level) {

	String id = String.format("%s_%d_%s", category.name(), metadata.getAID(),
		mol.getProperty(_field_top.PUBCHEM_SID.name()));

	String experimentUUID = prefix + UUID.nameUUIDFromBytes(id.getBytes());

	if (record.getMeasurements() != null)
	    for (ProtocolApplication<Protocol, IParams, String, IParams, String> experiment : record.getMeasurements()) {
		if (experimentUUID.equals(experiment.getDocumentUUID()))
		    return experiment;
	    }
	ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = category
		.createExperimentRecord(protocol);
	experiment.setReferenceYear(metadata.getAIDSource_year());
	experiment.setReference(String.format("https://pubchem.ncbi.nlm.nih.gov/rest/rdf/bioassay/AID%d.html",
		metadata.getAID()));
	experiment.setReliability(reliability);
	experiment.setParameters(new Params());
	experiment.setReferenceOwner(getReference().getName());

	// setCitation(experiment);
	experiment.setDocumentUUID(experimentUUID);
	record.addMeasurement(experiment);
	return experiment;
    }

    protected String parseTag(String thetag, EffectRecord<String, IParams, String> effect) {

	int ix = thetag.indexOf("W460-");
	if (ix == 0) {
	    Value v = new Value(460);
	    v.setUnits("nm");
	    effect.getConditions().put("Emission wavelength", v);
	    return thetag.substring(5);
	}
	ix = thetag.indexOf("W530-");
	if (ix == 0) {
	    Value v = new Value(530);
	    v.setUnits("nm");
	    effect.getConditions().put("Emission wavelength", v);
	    return thetag.substring(5);
	}
	ix = thetag.indexOf("W535-");
	if (ix == 0) {
	    Value v = new Value(535);
	    v.setUnits("nm");
	    effect.getConditions().put("Emission wavelength", v);
	    return thetag.substring(5);
	}
	ix = thetag.indexOf("W590-");
	if (ix == 0) {
	    Value v = new Value(590);
	    v.setUnits("nm");
	    effect.getConditions().put("Emission wavelength", v);
	    return thetag.substring(5);
	}
	ix = thetag.indexOf("530 nm ");
	if (ix == 0) {
	    Value v = new Value(530);
	    v.setUnits("nm");
	    effect.getConditions().put("Emission wavelength", v);
	    return thetag.substring(7);
	}
	ix = thetag.indexOf("535 nm ");
	if (ix == 0) {
	    Value v = new Value(535);
	    v.setUnits("nm");
	    effect.getConditions().put("Emission wavelength", v);
	    return thetag.substring(7);
	}
	ix = thetag.indexOf("590 nm ");
	if (ix == 0) {
	    Value v = new Value(590);
	    v.setUnits("nm");
	    effect.getConditions().put("Emission wavelength", v);
	    return thetag.substring(7);
	}
	ix = thetag.indexOf("460 nm ");
	if (ix == 0) {
	    Value v = new Value(460);
	    v.setUnits("nm");
	    effect.getConditions().put("Emission wavelength", v);
	    return thetag.substring(7);
	}
	/*
	 * ix = thetag.indexOf("Ratio-"); if (ix==0) { Value v = new
	 * Value("Ratio 460nm/530nm");
	 * effect.getConditions().put("Emission wavelength", v); return
	 * thetag.substring(6); }
	 */

	ix = thetag.indexOf("Blue (460 nm) auto fluorescence outcome");
	if (ix == 0) {
	    Value v = new Value(460);
	    v.setUnits("nm");
	    effect.getConditions().put("Emission wavelength", v);
	    return "Auto fluorescence outcome";
	}

	ix = thetag.indexOf("AhR ");
	if (ix == 0) {
	    Value v = new Value("Aryl hydrocarbon receptor");
	    // http://www.ncbi.nlm.nih.gov/protein/51095037
	    return thetag.substring(4);
	}

	ix = thetag.indexOf("TR ");
	if (ix == 0) {
	    Value v = new Value("Thyroid receptor");
	    return thetag.substring(3);
	}

	ix = thetag.indexOf("AR ");
	if (ix == 0) {
	    Value v = new Value("Androgen receptor");
	    // http://www.ncbi.nlm.nih.gov/protein/124375976
	    return thetag.substring(3);
	}

	ix = thetag.indexOf("ER ");
	if (ix == 0) {
	    Value v = new Value("Estrogen receptor alpha");
	    // http://www.ncbi.nlm.nih.gov/protein/348019627
	    return thetag.substring(3);
	}

	ix = thetag.indexOf("ATAD5 ");
	if (ix == 0) {
	    Value v = new Value("ATAD5");
	    // http://www.ncbi.nlm.nih.gov/protein/296439460
	    return thetag.substring(6);
	}

	ix = thetag.indexOf("Antagonist ");
	if (ix == 0) {
	    Value v = new Value("Aromatase inhibitor");
	    // http://www.ncbi.nlm.nih.gov/protein/119597822
	    return thetag.substring(10);
	}

	return thetag;
    }

    protected String parseReplicate(String thetag, EffectRecord<String, IParams, String> effect,
	    ProtocolApplication<Protocol, IParams, String, IParams, String> experiment) {
	int replicate_index = thetag.indexOf("-Replicate");
	if (replicate_index > 0) {
	    Value value = new Value(Integer.parseInt(thetag.substring(replicate_index).replace("-Replicate_", "")));
	    value.setLoQualifier("=");

	    effect.getConditions().put("Replicate", value);
	    // experiment.getParameters().put("Replicate", value);
	    return thetag.substring(0, replicate_index);
	}
	return thetag;
    }

    protected String parseActivity(String thetag, JsonNode tagNode, EffectRecord<String, IParams, String> effect) {

	JsonNode unitNode = tagNode == null ? null : tagNode.get("unit");
	if (unitNode != null) {
	    if ("none".equals(unitNode.textValue()))
		effect.setUnit(null);
	    else if ("percent".equals(unitNode.textValue()))
		effect.setUnit("%");
	    else
		effect.setUnit(unitNode.textValue());
	}
	// tested concentration
	JsonNode tcNode = tagNode == null ? null : tagNode.get("tc");
	if (tcNode != null && tcNode.get("concentration") != null) {
	    Value val = new Value(tcNode.get("concentration").doubleValue());
	    try {
		val.setUnits(tcNode.get("unit").textValue());
	    } catch (Exception x) {
		val.setUnits(null);
	    }
	    val.setLoQualifier("=");
	    if (tcNode.get("dr_id") != null) {
		val.setAnnotation(tcNode.get("dr_id").textValue());
		effect.setTextValue(tcNode.get("dr_id").textValue());
	    }
	    effect.getConditions().put(I5CONSTANTS.cDoses, val);
	}

	int ix = thetag.indexOf("Activity at");
	if (ix >= 0) {
	    /*
	     * String concentration = thetag.substring(ix+12); String[] c =
	     * concentration.split(" "); Value val = new
	     * Value(Double.parseDouble(c[0])); val.setUnits(c[1]);
	     * val.setLoQualifier("=");
	     * effect.getConditions().put(I5CONSTANTS.cDoses, val);
	     */

	    if (thetag.startsWith("Ratio-Activity at"))
		return _field.Ratio_Activity.name();
	    else
		return _field.Activity.name();
	}
	ix = thetag.indexOf("(%)");
	if (ix >= 0) {
	    // effect.setUnit("%");
	    return thetag.substring(0, ix - 1).trim();
	}
	ix = thetag.indexOf("(uM)");
	if (ix >= 0) {
	    // effect.setUnit("uM");
	    return thetag.substring(0, ix - 1).trim();
	}
	return thetag;
    }

    @Override
    protected Object transform(Object o) {
	if (o instanceof IAtomContainer)
	    try {

		r.clear();
		r.setFormat("SDF");
		java.util.Set<Object> keys = ((IAtomContainer) o).getProperties().keySet();

		I5_ROOT_OBJECTS category0 = I5_ROOT_OBJECTS.UNKNOWN_TOXICITY;
		I5_ROOT_OBJECTS category1 = I5_ROOT_OBJECTS.PUBCHEM_DOSERESPONSE;

		String title = metadata.getTitle();
		String uri = metadata.getURI();
		String cell = null;// metadata.get("cell")==null?null:metadata.get("cell").getTextValue();
		String target_name = metadata.getTargetName();
		String target_id = metadata.getTargetMolID() == null ? null : String.format(
			"https://pubchem.ncbi.nlm.nih.gov/rest/rdf/protein/GI%s", metadata.getTargetMolID());
		int aid = metadata.getAID();
		String assay_type = "PUBCHEM_" + metadata.getActivityOutcomeMethod().toUpperCase();

		try {
		    category0 = I5_ROOT_OBJECTS.valueOf(assay_type);
		} catch (Exception x) {
		}

		Protocol protocol0 = category0.getProtocol(metadata.getActivityOutcomeMethod());
		Protocol protocol1 = category1.getProtocol(metadata.getActivityOutcomeMethod());

		protocol0.addGuideline(metadata.getTitle() + "<p/>" + metadata.getProtocolAsText());
		// protocol1.addGuideline(metadata.getProtocolAsText());

		ReliabilityParams rel = new ReliabilityParams();
		rel.setStudyResultType("experimental result");

		ProtocolApplication<Protocol, IParams, String, IParams, String> experiment0 = getExperiment(category0,
			(IAtomContainer) o, (SubstanceRecord) r, protocol0, rel, 0);

		ProtocolApplication<Protocol, IParams, String, IParams, String> experiment1 = null;
		if (metadata.getDoseResponse() != null) {
		    experiment1 = getExperiment(category1, (IAtomContainer) o, (SubstanceRecord) r, protocol1, rel, 1);
		}

		if (target_id != null) {
		    // * http://www.ncbi.nlm.nih.gov/gene/?term=7157
		    // https://pubchem.ncbi.nlm.nih.gov/rest/rdf/gene/GID367
		    // Accept:application/json
		    // https://pubchem.ncbi.nlm.nih.gov/rdf/#_Toc376426185
		    experiment0.getParameters().put(I5CONSTANTS.cTargetGene, target_id);
		    if (experiment1 != null)
			experiment1.getParameters().put(I5CONSTANTS.cTargetGene, target_id);
		}
		if (cell != null) {
		    experiment0.getParameters().put("Cell", cell);
		    if (experiment1 != null)
			experiment1.getParameters().put("Cell", cell);
		}

		experiment0.setInterpretationCriteria(target_name);
		if (experiment1 != null)
		    experiment1.setInterpretationCriteria(target_name);

		Iterator i = keys.iterator();

		while (i.hasNext()) {

		    Object key = i.next();
		    String thetag = key.toString();
		    try {
			Object value = ((IAtomContainer) o).getProperty(key.toString());

			JsonNode tagNode = metadata.getTag(key.toString());
			if (value == null || "".equals(value.toString().trim()))
			    continue;

			try {
			    _field_top field = _field_top.valueOf(thetag.trim().replace(" ", "_").replace("-", "_")
				    .replace("%", "_").replace("(", "_").replace(")", "_"));
			    field.parse(key.toString(), experiment0, (SubstanceRecord) r, (IAtomContainer) o);
			    EffectRecord effect = category0.createEffectRecord();
			    addConditions(effect, target_id, target_name);
			    field.addEffectRecord(key.toString(), experiment0, effect, (SubstanceRecord) r,
				    (IAtomContainer) o);
			    if (isReadPubchemScoreOnly()) {
				// field.addEffectRecord(key.toString(),experiment0,category0.createEffectRecord(),(SubstanceRecord)r,
				// (IAtomContainer)o);
				continue;
			    }
			    continue;
			} catch (Exception x) {
			    // further processing
			}

			if (isReadPubchemScoreOnly()) {
			    continue;
			}

			EffectRecord<String, IParams, String> effect = category0.createEffectRecord();
			addConditions(effect, target_id, target_name);
			thetag = parseReplicate(thetag, effect, experiment0);

			_field field = null;
			thetag = parseTag(thetag, effect);

			thetag = parseActivity(thetag, tagNode, effect);
			if (thetag == null)
			    continue;

			field = _field.valueOf(thetag.trim().replace(" ", "_").replace("-", "_").replace("%", "_")
				.replace("(", "_").replace(")", "_"));

			field.parse(key.toString(), effect, experiment0, (SubstanceRecord) r, (IAtomContainer) o);

			if ((experiment1 != null) && (effect.getConditions().get(I5CONSTANTS.cDoses) != null))
			    experiment1.addEffect(effect);
			else
			    experiment0.addEffect(effect);

			if (effect.getEndpoint() == null) {
			    throw new Exception("Null effect");
			}
		    } catch (Exception x) {
			System.err.println(key);
			System.err.println(x.getMessage());
			// x.printStackTrace();
		    }

		    // r.setProperty(key,
		    // ((IAtomContainer)o).getProperties().get(key));
		}

		/*
		 * if (((SubstanceRecord)r).getCompanyUUID()==null)
		 * ((SubstanceRecord
		 * )r).setCompanyUUID(reader.getPrefix()+UUID.randomUUID());
		 * 
		 * //owner is the dataset
		 * ((SubstanceRecord)r).setOwnerName(reader
		 * .getReference().getName());
		 * ((SubstanceRecord)r).setOwnerUUID(reader.getPrefix() +
		 * UUID.nameUUIDFromBytes
		 * (reader.getReference().getURL().toString().getBytes()));
		 * //ids.add(new
		 * ExternalIdentifier("DOI","http://dx.doi.org/10.1021/nn406018q"
		 * ));
		 */
		MoleculeTools.clearProperties((IAtomContainer) o);
		r.setContent(writer.process((IAtomContainer) o));
		Object ref = ((IAtomContainer) o).getProperty("REFERENCE");
		if (ref instanceof LiteratureEntry)
		    r.setReference((LiteratureEntry) ref);
		else
		    r.setReference(getReference());

		return r;
	    } catch (Exception x) {
		x.printStackTrace();
		r.clear();
		r.setFormat("SDF");
		r.setContent(null);
		r.setReference(getReference());
		return r;
	    }
	else
	    return o;
    }

    protected void addConditions(EffectRecord<String, IParams, String> effect, String target_id, String target_name) {
	if (target_id != null) {
	    // * http://www.ncbi.nlm.nih.gov/gene/?term=7157
	    // https://pubchem.ncbi.nlm.nih.gov/rest/rdf/gene/GID367
	    // Accept:application/json
	    // https://pubchem.ncbi.nlm.nih.gov/rdf/#_Toc376426185
	    Value v = new Value(target_id);
	    v.setAnnotation(target_name);
	    effect.getConditions().put(I5CONSTANTS.cTargetGene, v);
	} else
	    effect.getConditions().put(I5CONSTANTS.cTargetGene, null);
    }

}