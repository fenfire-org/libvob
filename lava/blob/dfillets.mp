prologues := 2;

path circle, hcircle, qcircle;
circle = fullcircle scaled 2;
hcircle = halfcircle scaled 2;
qcircle = quartercircle scaled 2;

vardef fcircle(suffix into)(expr r, ang, rad, thic, di) =
    save v, p, q, ori;
    pair v, p, q, ori;
    v := dir(ang);
    % Passes through p at angle ang
    q := rad * v + di * (thic/2 - r) * (v rotated 90);
    % Origin of circle lies on two lines
    ori = q + whatever * (v rotated 90);
    ori = .5[q,(0,0)] + whatever * (q rotated 90);

    into.c := ori;
    into.radius := abs(ori - q) - r;
    into.pth := ((qcircle xscaled -1 yscaled -di 
		scaled into.radius
		rotated ang 
		shifted into.c)
		cutbefore ((0,0) -- into.c))
		;
    into.eang := angle(into.c);
enddef;

def declfcircle(suffix s) =
    pair s[].c;
    path s[].pth;
enddef;

vardef inters(expr p)(suffix s) =
    save i;
    pair i;
    i := p intersectiontimes s.pth;
    if xpart(i) >= 0:
	point ypart(i) of s.pth 
    else:
	(0,0)
    fi
enddef;

vardef interppnt(expr ang, bang, bfact, r)(suffix a, b) =
    save d, pd, ia, ib, fa, fb, fc;
    pair d;
    d := dir(ang);
    path pd, pb;
    pd = (0,0) -- 10 * r * d;
    pb = (0,0) -- 10 * r * dir(bang);

    pair ia, ib;
    ia = inters(pd , a);
    ib = inters(pb , b);
    numeric fa, fb;
    fa = abs(ia) - r;
    fb = abs(ib) - r;
    if fa < 0: fa := 0; fi
    if fb < 0: fb := 0; fi

    numeric fc;
    show fa, fb, bfact;
    fc := fa + bfact * fb + r;
    show fc,d,"C";
    d * fc

%    if abs(ia) > abs(ib):
%	ia
%    else:
%	ib
%    fi
enddef;

vardef interpdraw(expr r)(expr aa)(suffix a)(expr ab)(suffix b) =
    save pa;
    if aa > ab: errmessage("Dir"); fi

    save interse;
    pair interse;
    interse := a.pth intersectionpoint b.pth;
    

    path pa;
    numeric fact;
    if a.eang > ab:
	show ab, aa, a.eang,"X";
	fact := (ab - aa) / (a.eang - aa);
	fact := fact * fact * fact;
	show fact,"Y";

	save eange;
	numeric eange;
	eange := fact[a.eang, angle(interse)];

	pa := 
	for ang = aa+1 step .1 until eange:
	    interppnt(ang, ang - (a.eang - ab), fact, r, a, b) --
	endfor
	    interppnt(eange, eange - (a.eang - ab), fact, r, a, b);
    else:
	pa := 
	for ang = aa+1 step .1 until angle(interse):
	    interppnt(ang, ang, 1, r, a, b) --
	endfor
	    interppnt(angle(interse), angle(interse), 1, r, a, b);
    fi
    draw pa withpen pencircle scaled 3pt; 
enddef;

def diag(expr r, aa, ra, ta,  ab, rb, tb) =
    
    save a, b;
    declfcircle(a);
    declfcircle(b);
    fcircle(a[0], r, aa, ra, ta, 1);
    fcircle(a[1], r, aa, ra, ta, -1);
    fcircle(b[0], r, ab, rb, tb, 1);
    fcircle(b[1], r, ab, rb, tb, -1);

    draw circle scaled r;

%    draw circle scaled a0.radius shifted a0.c;

    draw a0.pth withpen pencircle scaled 1.5pt;

    draw a1.pth ;
    draw b0.pth ;

    draw b1.pth withpen pencircle scaled 1.5pt;
%    draw circle scaled a1.radius shifted a1.c;
%    draw circle scaled b0.radius shifted b0.c;
%    draw circle scaled b1.radius shifted b1.c;

    interpdraw(r, aa, a0, ab, b1);
%    currentpicture := currentpicture yscaled -1;

    % Blending curve: 



enddef;

beginfig(1);
    for an = 100 step 8 until 155:
	diag(100, 90, 200, 7, an, 150, 10);
	currentpicture := currentpicture shifted (-200, 0);
    endfor;
endfig;

end;
