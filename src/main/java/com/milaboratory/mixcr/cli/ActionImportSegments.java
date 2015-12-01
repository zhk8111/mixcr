/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.mixcr.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.milaboratory.mitools.cli.Action;
import com.milaboratory.mitools.cli.ActionHelper;
import com.milaboratory.mitools.cli.ActionParameters;
import com.milaboratory.mixcr.reference.LociLibrary;
import com.milaboratory.mixcr.reference.LociLibraryReader;
import com.milaboratory.mixcr.reference.Locus;
import com.milaboratory.mixcr.reference.SpeciesAndLocus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ActionImportSegments implements Action {
    private final AParameters params = new AParameters();

    @Override
    public void go(ActionHelper helper) throws Exception {
        Path outputFile = params.getOutputFile();
        Files.createDirectories(outputFile.getParent());
        boolean outputExists = Files.exists(outputFile);
        int taxonID = params.getTaxonID();
        Locus locus = params.getLocus();
        String[] commonNames = params.getCommonNames();
        if (outputExists) {
            LociLibrary ll = LociLibraryReader.read(outputFile.toFile(), false);
            SpeciesAndLocus sl = new SpeciesAndLocus(taxonID, locus);
            if (ll.getLocus(sl) != null) {
                System.err.println("Specified file already contain record for: " + sl);
                return;
            }
            for (String commonName : commonNames) {
                int id = ll.getSpeciesTaxonId(commonName);
                if (id != -1 && id != taxonID) {
                    System.err.println("Specified file contains other mapping for common species name: " + commonName + " -> " + id);
                    return;
                }
            }
        }



        //FastaLocusBuilder vBuilder = new FastaLocusBuilder(locus, )
    }

    @Override
    public String command() {
        return "importSegments";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Imports segment sequences from fasta file (e.g. formatted as IMGT reference " +
            "sequences with IMGT gaps).",
            optionPrefixes = "-")
    public static final class AParameters extends ActionParameters {
        //@Parameter(description = "input_file_V.fasta input_file_J.fasta [input_file_D.fasta]")
        //public List<String> parameters;

        @Parameter(description = "Import parameters (name of builtin parameter set of name of JSON file with custom " +
                "import parameters).", names = {"-p", "--parameters"})
        public String assemblerParametersName = "imgt";

        @Parameter(description = "Input *.fasta file with V genes.",
                names = {"-v"})
        public String v;

        @Parameter(description = "Input *.fasta file with J genes.",
                names = {"-j"})
        public String j;

        @Parameter(description = "Input *.fasta file with D genes.",
                names = {"-d"})
        public String d;

        @Parameter(description = "Locus (e.g. IGH, TRB etc...)",
                names = {"-l", "--locus"})
        public String locus;

        @Parameter(description = "Species taxonID and it's common names (e.g. 9606:human:HomoSapiens:hsa)",
                names = {"-s", "--species"})
        public String species;

        @Parameter(description = "Report file.",
                names = {"-r", "--report"})
        public String report;

        @Parameter(description = "Output file (optional, default path is ~/.mixcr/local.ll", //, or $MIXCR_PATH/system.ll if -g option specified)",
                names = {"-o", "--output"})
        public String output = null;

        //@Parameter(description = "Add to system-wide loci library ($MIXCR_PATH/system.ll).",
        //        names = {"-s", "--system"}, hidden = true)
        //public Boolean global;

        public Locus getLocus() {
            return Locus.fromId(locus);
        }

        public int getTaxonID() {
            String[] split = species.split("\\:");
            return Integer.parseInt(split[0]);
        }

        public String[] getCommonNames() {
            String[] split = species.split("\\:");
            return Arrays.copyOfRange(split, 1, split.length);
        }

        public Path getOutputFile() {
            if (output != null)
                return Paths.get(output);
            return Util.getLocalSettingsDir().resolve("local.ll");
        }

        public String getV() {
            return v;
        }

        public String getJ() {
            return j;
        }

        public String getD() {
            return d;
        }

        @Override
        public void validate() {
            if (v == null)
                throw new ParameterException("Please specify file for V gene.");

            if (j == null)
                throw new ParameterException("Please specify file for J gene.");

            if (locus == null)
                throw new ParameterException("Please specify locus (e.g. \"-l TRB\").");
            if (Locus.fromId(locus) == null)
                throw new ParameterException("Unrecognized locus: " + locus);

            if (species == null)
                throw new ParameterException("Please specify species.");

            try {
                Integer.parseInt(species.split(":")[0]);
            } catch (NumberFormatException e) {
                throw new ParameterException("Malformed species name.");
            }

            super.validate();
        }
    }
}
