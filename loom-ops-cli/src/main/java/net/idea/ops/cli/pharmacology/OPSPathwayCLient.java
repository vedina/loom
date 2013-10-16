package net.idea.ops.cli.pharmacology;

public class OPSPathwayCLient {

}
/*
 {
 "format": "linked-data-api",
 "version": "0.2",
 "result": {
  "_about": "https://beta.openphacts.org/pathway?uri=http%3A%2F%2Frdf.wikipathways.org%2FWP1019_r48131&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_format=json",
  "definition": "https://beta.openphacts.org/api-config",
  "extendedMetadataVersion": "https://beta.openphacts.org/pathway?uri=http%3A%2F%2Frdf.wikipathways.org%2FWP1019_r48131&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_format=json&_metadata=all%2Cviews%2Cformats%2Cexecution%2Cbindings%2Csite",
  "primaryTopic": {
   "_about": "http://rdf.wikipathways.org/WP1019_r48131",
   "identifier": "http://identifiers.org/wikipathways/WP1019_r48131",
   "title_en": "FAS pathway and Stress induction of HSP regulation",
   "title": "FAS pathway and Stress induction of HSP regulation",
   "description": "This pathway describes the Fas induced apoptosis and interplay with Hsp27 in response to stress.\n\nMore info: [http://www.biocarta.com/pathfiles/h_hsp27Pathway.asp BioCarta].",
   "hasPart": [
    {
     "_about": "http://internal.wikipathways.org/noDataSource/dba3f855-8a1b-4eb7-bd8d-aa2d56f81170",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:533729",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:100140945",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://www.hmdb.ca/metabolites/HMDB00125",
     "type": "http://vocabularies.wikipathways.org/wp#Metabolite"
    },
    {
     "_about": "http://bio2rdf.org/geneid:526279",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:286764",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:516326",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:534712",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:404144",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:523962",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:281020",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:512740",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://identifiers.org/pubmed/10914538",
     "type": "http://vocabularies.wikipathways.org/wp#PublicationReference"
    },
    {
     "_about": "http://bio2rdf.org/geneid:529146",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:540108",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:512730",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://www.hmdb.ca/metabolites/HMDB01429",
     "type": "http://vocabularies.wikipathways.org/wp#Metabolite"
    },
    {
     "_about": "http://bio2rdf.org/geneid:493720",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:507981",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:615215",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:540643",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:282488",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:513673",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:280831",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:497199",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:537782",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://internal.wikipathways.org/noDataSource/a259aa9b-9780-40f4-b5a5-e5f891fd4544",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:788091",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:531770",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:538409",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:504336",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:407111",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:516099",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:327676",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:539941",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:507481",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:534407",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:280943",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://internal.wikipathways.org/noDataSource/d02fcee7-71fb-4e51-a193-4ddac5bd9341",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:526469",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://commonchemistry.org/ChemicalDetail.aspx?ref=104404-17-3",
     "type": "http://vocabularies.wikipathways.org/wp#Metabolite"
    },
    {
     "_about": "http://bio2rdf.org/geneid:281250",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://internal.wikipathways.org/noDataSource/6a737eba-1448-47d1-8a60-21c372d196a7",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    },
    {
     "_about": "http://bio2rdf.org/geneid:408016",
     "type": "http://vocabularies.wikipathways.org/wp#GeneProduct"
    }
   ],
   "inDataset": "http://www.wikipathways.org",
   "organism": {
    "_about": "http://identifiers.org/taxonomy/9913",
    "label": "Bos taurus"
   },
   "pathwayOntology": "http://purl.obolibrary.org/obo/PW:0000237",
   "isPrimaryTopicOf": "https://beta.openphacts.org/pathway?uri=http%3A%2F%2Frdf.wikipathways.org%2FWP1019_r48131&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_format=json",
   "page": "http://www.wikipathways.org/index.php/Pathway:WP1019_r48131"
  }
 }
}
*/