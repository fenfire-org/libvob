prologues := 2;

path circle;
circle = fullcircle scaled 2;

boolean helpers;
helpers = false;

def diag(expr r, d, h) =
    pair p, q;
    p = (d,h);
    q = p - (0, r);
    pair c;
    c = p + whatever * (0,1);
    c = .5[(0,0), q] + whatever * (q rotated 90);
    
    path circ;
    circ = halfcircle rotated 180 scaled (2* abs(c-p)) shifted c;

    circ:=  (circ cutbefore ((0,0) -- c) );
    circ:=  (circ cutafter ((2*d,0) -- c) );


    path ca;
    ca = halfcircle scaled (2*r);
    ca := (ca cutbefore ((0,0) -- c));
    path cb;
    cb := ca xscaled -1 shifted (2*d,0);

    draw circ;
    draw ca;
    draw cb;

    draw circ yscaled -1;
    draw ca yscaled -1;
    draw cb yscaled -1;

    if helpers:
	draw (d-10,h) -- (d+10,h);
	draw (0,0) -- (d,0) withpen pencircle scaled .5pt;
	draw circle scaled r;
	draw circle  scaled r shifted (2*d,0);

	dotlabel.lft("p", p);
	dotlabel.lft("q", q);
	draw (0,0) -- q withpen pencircle scaled .5pt;
	dotlabel.lft("c", c);

	draw .5[(0,0), q] -- c withpen pencircle scaled .5pt;
    fi;

enddef;

beginfig(1);

picture pict;
pict = nullpicture;
for d = 100, 150, 200, 250:
    for h = 1, 5, 10, 20:
	currentpicture := nullpicture;
	diag(50, d, h);
	addto pict also currentpicture shifted ((d-100)*10, 
				    h * 40);
    endfor;
endfor;
currentpicture := pict;

%clip currentpicture to unitsquare scaled 2000 shifted (-250,-250);

endfig;

end
