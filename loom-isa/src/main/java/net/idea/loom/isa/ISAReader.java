package net.idea.loom.isa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.CharacteristicValue;
import uk.ac.ebi.bioinvindex.model.term.Factor;
import uk.ac.ebi.bioinvindex.model.term.FactorValue;
import uk.ac.ebi.bioinvindex.model.term.OntologyTerm;
import uk.ac.ebi.bioinvindex.model.term.ParameterValue;
import ambit2.base.data.ILiteratureEntry;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ReliabilityParams;
import ambit2.base.data.study.Value;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.interfaces.ICiteable;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.IRawReader;

public class ISAReader extends DefaultIteratingChemObjectReader implements IRawReader<IStructureRecord>, ICiteable {

    protected BIIObjectStore store;
    protected Iterator<Identifiable> studyIterator;
    protected Iterator<AssayResult> assayResultIterator;
    protected Collection<Identifiable> studies;

    public ISAReader(File directory) throws Exception {
	try {
	    ISATABLoader loader = new ISATABLoader(directory.getAbsolutePath());
	    FormatSetInstance isatabInstance = loader.load();

	    ISATABValidator validator = new ISATABValidator(isatabInstance);

	    if (GUIInvokerResult.WARNING == validator.validate()) {
		// vlog.warn("ISA-Configurator Validation reported problems, see the messages above or the log file");
	    }
	    store = validator.getStore();
	    studies = new ArrayList<Identifiable>();
	    studies.addAll(store.values(Study.class));
	    studyIterator = studies.iterator();
	    assayResultIterator = null;
	    // parse();
	} catch (Exception x) {
	    throw x;
	} finally {
	    // anything to close?
	}
    }

    @Override
    public boolean hasNext() {
	boolean hasNext = false;
	if (assayResultIterator == null) {
	    if (studyIterator.hasNext()) {
		Identifiable object = studyIterator.next();
		assayResultIterator = ((Study) object).getAssayResults().iterator();
		hasNext = assayResultIterator.hasNext();
	    }
	} else {
	    hasNext = assayResultIterator.hasNext();
	    if (!hasNext) { // go to outer loop
		if (studyIterator.hasNext()) {
		    Identifiable object = studyIterator.next();
		    assayResultIterator = ((Study) object).getAssayResults().iterator();
		    hasNext = assayResultIterator.hasNext();
		}
	    }
	}
	return hasNext;
    }

    @Override
    public Object next() {
	return (assayResultIterator == null) ? null : assayResultIterator.next();
    }

    @Override
    public IStructureRecord nextRecord() {
	AssayResult result = (AssayResult) next();
	// System.out.println(result);
	return parseAssayResult(result);
    }

    protected SubstanceRecord parseAssayResult(AssayResult result) {
	if (result == null)
	    return null;

	Protocol a_protocol = null;
	System.out.println("Assay result\t" + result.getId());
	for (Assay assay : result.getAssays()) {
	    /*
	     * System.out.println(assay.getAcc());
	     * System.out.println(assay.getTechnologyName());
	     * System.out.println(assay.getTechnology());
	     * System.out.println(assay.getMeasurement());
	     */
	    a_protocol = new Protocol(assay.getMeasurement().getName());
	    a_protocol.setTopCategory("TOX");
	    // a_protocol.setCategory(assay.getTechnology().getName());
	    a_protocol.setCategory(assay.getMeasurement().getName().toUpperCase().replace(" ", "_"));// I5_ROOT_OBJECTS.UNKNOWN_TOXICITY.name());
	    System.out.println("Assay\t" + assay.getAcc());
	    System.out.println("\t" + assay.getMeasurement().getName());
	    System.out.println("\t" + assay.getTechnology().getName());

	}

	Params params = new Params();
	Params conditions = new Params();
	SubstanceRecord record = new SubstanceRecord();

	EffectRecord effect = new EffectRecord();
	effect.setEndpoint(result.getData().getName());
	if (result.getData().getDataMatrixUrl() != null)
	    effect.setTextValue(result.getData().getDataMatrixUrl());
	else
	    effect.setTextValue(result.getData().getUrl());
	effect.setConditions(conditions);
	ambit2.base.data.study.ProtocolApplication a_papp = new ambit2.base.data.study.ProtocolApplication(a_protocol);
	trackAssay(a_papp, result.getData().getProcessingNode(), record, params, effect, 0);

	if (record.getSubstanceUUID() == null) {
	    record.setPublicName("Dummy substance");
	    record.setSubstanceName("Dummy substance");
	    record.setSubstanceUUID("ISTB-" + UUID.nameUUIDFromBytes("Dummy substance".getBytes()));
	    record.setOwnerName("Unknown");
	    record.setOwnerUUID("ISTB-" + UUID.nameUUIDFromBytes("Unknown".getBytes()));
	    record.setFormat("ISATAB");
	    record.setSubstancetype("dummy");
	    record.setExternalids(null);
	}
	/*
	 * UUID docuuid = UUID.nameUUIDFromBytes( (a_protocol +
	 * params.toString()) .getBytes() );
	 */
	UUID docuuid = UUID.randomUUID();

	a_papp.setDocumentUUID("ISTB-" + docuuid);
	a_papp.setReference(result.getStudy().getTitle());
	a_papp.setReferenceOwner("test");
	try {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(result.getStudy().getReleaseDate());
	    a_papp.setReferenceYear(Integer.toString(calendar.get(Calendar.YEAR)));
	} catch (Exception x) {
	}

	for (Contact contact : result.getStudy().getContacts()) {
	    a_papp.setReferenceOwner(contact.getUrl());
	}
	a_papp.setParameters(params);

	a_papp.setCompanyName(record.getOwnerName());
	a_papp.setCompanyUUID(record.getOwnerUUID());
	a_papp.setSubstanceUUID(record.getSubstanceUUID());

	a_papp.addEffect(effect);
	ReliabilityParams reliability = new ReliabilityParams();
	reliability.setStudyResultType("experimental result");
	a_papp.setReliability(reliability);

	record.addMeasurement(a_papp);
	return record;
    }

    protected int parse() throws Exception {

	/*
	 * Iterator<Class<? extends Identifiable>> i = store.types().iterator();
	 * while (i.hasNext()) { Class<? extends Identifiable> c = i.next();
	 * System.out.println("=================================");
	 * System.out.println(c.getName());
	 * 
	 * objects.addAll(store.values(c)); for ( Identifiable object: objects )
	 * { System.out.print("\t"); System.out.println(object); }
	 * objects.clear(); }
	 */
	/*
	 * int r = 0; while (hasNext()) { parseAssayResult((AssayResult)
	 * next()); r++; } return r;
	 */
	return 0;

    }

    protected void processFactorValues(Collection<FactorValue> factorvalues, Params params, SubstanceRecord record) {
	for (FactorValue pv : factorvalues) {
	    Factor f = pv.getType();
	    if ("compound".equals(f.getValue()) || "limiting nutrient".equals(f.getValue())) {
		OntologyTerm term = pv.getSingleOntologyTerm();
		record.setPublicName(pv.getValue().toLowerCase());
		if (term != null) {
		    record.setSubstanceName(term.getAcc());
		    record.setSubstanceUUID("ISTB-" + UUID.nameUUIDFromBytes(term.getAcc().getBytes()));
		    record.setOwnerName(term.getSource().getUrl());
		    record.setOwnerUUID("ISTB-" + UUID.nameUUIDFromBytes(term.getSource().getName().getBytes()));
		    record.setFormat("ISATAB");
		    List<ExternalIdentifier> ids = new ArrayList<ExternalIdentifier>();
		    record.setExternalids(ids);
		    record.setSubstancetype("compound");
		    ids.add(new ExternalIdentifier(term.getSource().getUrl() == null ? "ISA-TAB" : term.getSource()
			    .getUrl(), term.getAcc()));
		} else {
		    byte[] name = record.getPublicName().getBytes();
		    record.setSubstanceName(record.getPublicName());
		    record.setSubstanceUUID("ISTB-" + UUID.nameUUIDFromBytes(name));
		    record.setOwnerName("ISA-TAB");
		    record.setOwnerUUID("ISTB-" + UUID.nameUUIDFromBytes("ISA-TAB".getBytes()));
		    record.setFormat("ISATAB");
		    record.setSubstancetype("compound");
		}
	    } else {
		Value factor = new Value();
		try {
		    if (pv.getUnit() != null)
			factor.setUnits(pv.getUnit().getValue());
		} catch (Exception x) {
		}
		try {
		    factor.setLoValue(Double.parseDouble(pv.getValue()));
		} catch (Exception x) {
		    factor.setLoValue(pv.getValue());
		}
		params.put(pv.getType().getValue(), factor);
		System.out.println(String.format("\t\tFactor\t%s = %s", pv.getType().getValue(), factor.getLoValue()));
	    }
	}
    }

    protected void processCharacteristicValues(Collection<CharacteristicValue> characteristicValues, Params params) {
	for (CharacteristicValue pv : characteristicValues) {
	    if ("Date".equals(pv.getType().getValue()))
		continue;
	    if ("Performer".equals(pv.getType().getValue()))
		continue;

	    Value value = new Value();
	    try {
		if (pv.getUnit() != null)
		    value.setUnits(pv.getUnit().getValue());
	    } catch (Exception x) {
	    }
	    try {
		value.setLoValue(Double.parseDouble(pv.getValue()));
	    } catch (Exception x) {
		value.setLoValue(pv.getValue());
	    }
	    params.put(pv.getType().getValue(), value);
	    System.out.println(String.format("\t\tCharacteristics\t%s = %s", pv.getType().getValue(),
		    value.getLoValue()));
	}
    }

    protected void processParamValues(Collection<ParameterValue> paramValues, Params params) {
	for (ParameterValue pv : paramValues) {
	    if ("Date".equals(pv.getType().getValue()))
		continue;
	    if ("Performer".equals(pv.getType().getValue()))
		continue;
	    Value value = new Value();
	    try {
		if (pv.getUnit() != null)
		    value.setUnits(pv.getUnit().getValue());
	    } catch (Exception x) {
	    }
	    try {
		value.setLoValue(Double.parseDouble(pv.getValue()));
	    } catch (Exception x) {
		value.setLoValue(pv.getValue());
	    }
	    params.put(pv.getType().getValue(), value);
	    System.out.println(String.format("\t\tParam\t%s = %s", pv.getType().getValue(), value.getLoValue()));
	}
    }

    protected void trackAssay(ambit2.base.data.study.ProtocolApplication a_papp,
	    uk.ac.ebi.bioinvindex.model.processing.Node node, SubstanceRecord record, Params protocolParams,
	    EffectRecord effect, int level) {
	if (node instanceof MaterialNode) {
	    System.out.print("\tMaterial node\tLevel " + level + " ");
	    System.out.println(node.getAcc());
	    processFactorValues(((MaterialNode) node).getMaterial().getFactorValues(), (Params) effect.getConditions(),
		    record);
	    processCharacteristicValues(((MaterialNode) node).getMaterial().getCharacteristicValues(), protocolParams);
	} else if (node instanceof DataNode) {
	    System.out.print("\tData node\tLevel " + level + "\t");
	    System.out.print("File " + ((DataNode) node).getData().getUrl() + "\t");
	    System.out.print("Data matrix " + ((DataNode) node).getData().getDataMatrixUrl() + "\t");
	    System.out.println(node.getAcc());
	    processFactorValues(((DataNode) node).getData().getFactorValues(), (Params) effect.getConditions(), record);
	}

	if (node.getDownstreamProcessings() == null)
	    return;
	for (Object processing : node.getDownstreamProcessings())
	    if (processing instanceof Processing) {
		Collection papps = ((Processing) processing).getProtocolApplications();
		if (papps != null)
		    for (Object p : papps)
			if (p instanceof ProtocolApplication) {
			    // assay protocol params fo to conditions; study
			    // protocol params go to protocol parameters
			    ProtocolApplication pa = (ProtocolApplication) p;
			    boolean assayField = true;
			    for (uk.ac.ebi.bioinvindex.model.Annotation annotation : pa.getAnnotations()) {
				if ("sampleFileId".equals(annotation.getType().getValue())) {
				    assayField = false;
				    break;
				}
			    }
			    if (assayField)
				processParamValues(pa.getParameterValues(), (Params) effect.getConditions());
			    else
				processParamValues(pa.getParameterValues(), protocolParams);

			    uk.ac.ebi.bioinvindex.model.Protocol protocol = ((ProtocolApplication) p).getProtocol();
			    ((Protocol) a_papp.getProtocol()).addGuideline(protocol.getName());
			    System.out.println(protocol.getName());

			}
		/*
		 * for (Object out : ((Processing) processing).getOutputNodes())
		 * { System.out.println(out); }
		 */
		System.out.println("-------------");
		for (Object in : ((Processing) processing).getInputNodes()) {
		    EffectRecord effect1 = effect;
		    if (in instanceof DataNode) {
			effect1 = new EffectRecord();
			effect1.setConditions(new Params());
			Data data = ((DataNode) in).getData();

			if (data.getDataMatrixUrl() != null) {
			    effect1.setTextValue(data.getDataMatrixUrl());
			    effect1.setEndpoint(data.getName());
			} else {
			    effect1.setTextValue(data.getUrl());
			    effect1.setEndpoint(data.getName());
			}
			a_papp.addEffect(effect1);
		    }
		    trackAssay(a_papp, (uk.ac.ebi.bioinvindex.model.processing.Node) in, record, protocolParams,
			    effect1, level - 1);
		}
	    }
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
    public void setReader(Reader reader) throws CDKException {
	throw new CDKException("not supported");
    }

}
