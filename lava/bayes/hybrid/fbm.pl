use PDL;
use PDL::Graphics::TriD;

# use PDL::Graphics::PGPLOT::Window;
# $win = PDL::Graphics::PGPLOT::Window->new( { Dev => "/XSERVE" } );

sub psi_str { # XXX Note no parens
    my($x, $t) = @_;
    return "
	    -Log(Abs($t - Exp(
	      - Exp(Abs(v) * Log(0.69314718055994529 * $x / Exp(2*u))) )))
    "
}

sub psi {
    my($u, $v, $x, $t) = @_;
    return -log(abs($t-exp(
	    -exp(abs($v) * log(0.69314718055994529 *$x / exp(2*$u))) )));
}

# Run a shell command, watch for errors
sub r {
    my ($cmd) = @_;
    $cmd =~ s/[\n\t]/ /g;
    system($cmd) == 0 or die "System failed: $? $cmd";
}

unlink "psitest.log";


r("dist-spec psitest.log
    'u ~ Normal(0, 100) + v ~ Normal(0, 100)'
    '@{[psi_str('i', 't')]}'");
r("data-spec psitest.log 1 1 / test.dat .");
r("mc-spec psitest.log heatbath hybrid 15 .05 ");


sub upto {
    my($iter) = @_;
    r("dist-mc psitest.log $iter");
    if(1) {

	my ($u, $v) = rcols( "dist-tbl uv psitest.log 100:%2|" );
	print "PARS:$u \n\n$v\n";

	$x = zeroes(1,100)->ylinvals(.01,20);

	$y = psi($u, $v, $x, 1);
	$ye = exp(-$y);
	# $ye->where(! $y->isfinite() ) .= -1;

	line3d [$ye->xchg(0,1)->yvals, $x->xchg(0,1), $ye->xchg(0,1)];
    }

    print "UV AUTOCOR\n";
    r("dist-tbl u psitest.log | series msac 50");
    r("dist-tbl v psitest.log | series msac 50");

}

upto(2000);
