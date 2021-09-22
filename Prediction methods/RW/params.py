"""Parsing the parameters."""

import argparse



def parameter_parser():
    """
    A method to parse up command line parameters.
    """

    parser = argparse.ArgumentParser(description="Run RW2.")

    parser.add_argument("--edgelist",
                        nargs="?",
                        #default="../../graphs/"+dataset+"/barabasi_ppis_nometanode.tab",  #edgelist.tab",
                        default = "data/HuRI.tsv",  # edgelist.tab",
                        help="Input graph path -- edge list tab file")

    parser.add_argument("--attributedwalk",
                        type = bool,
                        default = True,
                        help = "Use attributed walk or classic node2vec")

    parser.add_argument("--content",
                        nargs = "?",
                        #default = "../../graphs/"+dataset+'/barabasi_labels_bank_plus_nometanode.pickle',  #labels_drugbank_plus.pickle',
                        default = "data/huri_labels.pickle",
                        help = "Input node features.")

    parser.add_argument("--output",
                        nargs="?",
                        default="embeddings/embeddings_induced",
	                help="Embeddings path.")

    parser.add_argument("--fillempty",
                        type = bool,
                        default = False,
                        help = "Encode node and attributes labels to int. Default is True")

    parser.add_argument("--placeholder",
                        type = str,
                        default = None,#'*-TOKEN-*'
                        help = "Encode node and attributes labels to int. Default is None")

    parser.add_argument("--encode",
                        type = bool,
                        default = False,
                        help = "Encode node and attributes labels to int. Default is True")

    parser.add_argument("--directed",
                        type = bool,
                        default = False,
                        help = "Is the graph direct? Default is False")

    parser.add_argument("--window-size",
                        type=int,
                        default=7,#5,
	                help="Window size for skip-gram. Default is 5.")

    parser.add_argument("--walk-number",
                        type=int,
                        default= 50,#250,
	                help="Number of random walks. Default is 250.")

    parser.add_argument("--walk-length",
                        type=int,
                        default=20,
	                help="Walk length. Default is 20.")

    parser.add_argument("--P",
                        type=float,
                        default=1.,
	                help="Return parameter. Default is 1.0.")

    parser.add_argument("--Q",
                        type=float,
                        default=1.,
	                help="Inout parameter. Default is 1.0.")

    parser.add_argument("--dimensions",
                        type=int,
                        default=100,#128,
	                help="Number of dimensions. Default is 128.")

    parser.add_argument("--min-count",
                        type=int,
                        default=0,#0,
	                help="Minimal feature count. Default is 0.")

    parser.add_argument("--workers",
                        type=int,
                        default=40,
	                help="Number of cores. Default is 4.")

    parser.add_argument("--epochs",
                        type=int,
                        default=5,#5,#10,
	                help="Number of epochs. Default is 10.")

    parser.add_argument("--sg",
                        type = int,
                        default = 1, #1,
                        help = "sg. Default is 1")

    return parser.parse_args()
