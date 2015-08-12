package net.idea.ops.cli.assay;

import net.idea.opentox.cli.AbstractURLResource;
import net.idea.opentox.cli.dataset.Dataset;
import net.idea.opentox.cli.id.IIdentifier;
import net.idea.opentox.cli.structure.Compound;

public class AssayResult extends AbstractURLResource {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8776901044465176657L;
	Compound compound;
	Activity activity;
	Assay assay;
	Dataset inDataset;
	Citation citation;

	String relation;
	Double standardValue;
	String standardUnits;
	Double activityValue;

	public Double getActivityValue() {
		return activityValue;
	}

	public void setActivityValue(Double activityValue) {
		this.activityValue = activityValue;
	}

	public String getStandardUnits() {
		return standardUnits;
	}

	public void setStandardUnits(String standardUnits) {
		this.standardUnits = standardUnits;
	}

	public Citation getCitation() {
		return citation;
	}

	public void setCitation(Citation citation) {
		this.citation = citation;
	}

	public Compound getCompound() {
		return compound;
	}

	public void setCompound(Compound compound) {
		this.compound = compound;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Assay getAssay() {
		return assay;
	}

	public void setAssay(Assay assay) {
		this.assay = assay;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public Double getStandardValue() {
		return standardValue;
	}

	public void setStandardValue(Double standardValue) {
		this.standardValue = standardValue;
	}

	public Dataset getInDataset() {
		return inDataset;
	}

	public void setInDataset(Dataset inDataset) {
		this.inDataset = inDataset;
	}

	public AssayResult() {
		super(null);
	}

	public AssayResult(IIdentifier url) {
		super(url);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Assay Result:");
		if (getInDataset() != null) {
			b.append("\n\tIn dataset:\t");
			b.append(getInDataset().getResourceIdentifier());
		}
		b.append("\n\t");
		b.append(getRelation());
		b.append("\t");
		b.append(getStandardValue());
		if (getStandardUnits() != null) {
			b.append("\t");
			b.append(getStandardUnits());
		}

		if (getActivityValue() != null) {
			b.append("\n\tActivity value:\n\t\t");
			b.append(getActivityValue());
		}

		b.append("\n\tActivity:\n\t\t");
		b.append(getActivity());
		b.append("\n\t\t");
		if (getActivity().getType() != null) {
			b.append(getActivity().getType().getLabel());
			b.append("\t");
			b.append(getActivity().getType().getResourceIdentifier());
		}

		b.append("\n\tAssay:\n\t\t");
		b.append(getAssay().getResourceIdentifier());
		if (getAssay().getDescription() != null) {
			b.append("\n\t\t");
			b.append(getAssay().getDescription());
		}
		if (getAssay().getOrganism() != null) {
			b.append("\n\t\t");
			b.append(getAssay().getOrganism());
		}

		if (getAssay().getTarget() != null) {
			b.append("\n\tTarget:\n\t\t");
			b.append(getAssay().getTarget().getResourceIdentifier());
			if (getAssay().getTarget().getTitle() != null) {
				b.append("\n\t\t");
				b.append(getAssay().getTarget().getTitle());
			}
			if (getAssay().getTarget().getPrefLabel() != null) {
				b.append("\n\t\t");
				b.append(getAssay().getTarget().getPrefLabel());
			}
			if (getAssay().getTarget().getPrefLabelEN() != null) {
				b.append("\n\t\t");
				b.append(getAssay().getTarget().getPrefLabelEN());
			}
			if (getAssay().getTarget().getInDataset() != null) {
				b.append("\n\t\tIn dataset:\t");
				b.append(getAssay().getTarget().getInDataset()
						.getResourceIdentifier());
			}
		}

		b.append("\n\tCompound:\n\t\t");
		b.append(compound.getResourceIdentifier());
		b.append("\n\t\t");
		b.append(compound.getInChI());
		b.append("\n\t\t");
		b.append(compound.getInChIKey());
		b.append("\n\t\t");
		b.append(compound.getSMILES());
		b.append("\n\t\t");
		b.append(compound.getName());

		return b.toString();
	}

}
