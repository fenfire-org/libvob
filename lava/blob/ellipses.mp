prologues := 2;

vardef ellipsePoint(expr a, b, s, d) =
    save t, u, w, r;
    pair t, u, w;
    t = a;
    u = a+s*dir(d);
    for r = 0 upto 32:
	w := .5[t,u];
	if ((abs(w-a) + abs(w-b)) < s):
	    t := w;
	else:
	    u := w;
	fi
    endfor
    w
enddef;


def drawEllipseFoo(expr a, b, s) =
    draw 
    for d = 2 step 20 until 360:
	ellipsePoint(a, b, s, d) ..
    endfor
	cycle;
enddef;

vardef drawEllipse(expr a, b, s) =
save c, v, w;
pair c, v, w;
    c := .5[a,b];
    v := (a-b) / abs(a-b);
    w := (ypart(v), -xpart(v));
    draw c + .5*s * v --
         c - .5*s * v ;
%    draw c +  w * ((.5*s) +-+ (.5*abs(a-b))) --
%	 c -  w * ((.5*s) +-+ (.5*abs(a-b))) ;
    draw fullcircle scaled 2 
	    xscaled (.5*s)
	    yscaled ((.5*s) +-+ (.5*abs(a-b)))
	    rotated (angle(v))
	    shifted c;
enddef;

boolean drawhelp;
drawhelp = false;

% Given two points, normals at them and
% a distance from x to the first focal point,
% construct the (one or two) possible ellipses
def ellipseFrom(expr x, vx, y, vy, r) =
    dotlabel.lft("x", x);
    dotlabel.lft("y", y);
    drawarrow x -- x + vx;
    drawarrow y -- y + vy;

    if drawhelp:
	draw x+(0,r) .. x+(r,0) .. x+(0,-r) .. x+(-r,0) .. cycle withpen pencircle scaled .01pt;;
    fi

%    for i = -90 step 20 until 90:
    pair vyn, vxn;
    vyn = .01 * vy / abs(vy);
    vxn = .01 * vx / abs(vx);
    for i = 0 step .5 until 360:
	nof := i/360;
	pair v[];
	pair w[];
	v0 = dir(i);
	pair f[];
	f0 := x + v0 * r;
	w0 = f0 - y;
	v1 = dir(2*angle(vx) - i);
	w1 = dir(2*angle(vy) - angle(w0));


	if (dir(angle(v1)) dotprod dir(angle(w1) + 90)) > .04 :

	    f1 = x + whatever * v1;
	    f1 = y + whatever * w1;

	    numeric d[];
	    d0 = abs(x - f0) + abs(x - f1);
	    d1 = abs(y - f0) + abs(y - f1);

	    if (abs(d0 - d1) < .5) 
		and ((((y-f0) dotprod vyn) * ((y-f1) dotprod vyn)) > 0) 
		and ((((x-f0) dotprod vxn) * ((x-f1) dotprod vxn)) > 0) 
		:
		drawoptions(withcolor (nof*nof, (1-nof)*(1-nof), 2*nof*(1-nof)));
		if drawhelp:
		    draw x -- f0 withpen pencircle scaled .01pt;
		    draw y -- f0 withpen pencircle scaled .01pt;
		    draw x - 2.5 * r * v1 -- x + 2.5 * r * v1 withpen pencircle scaled .01pt;
		    draw y - 2.5 * r * w1 -- y + 2.5 * r * w1 withpen pencircle scaled .01pt;
		fi;
		dotlabel.lft("f1", f1);
		dotlabel.lft("f0", f0);
		drawEllipse(f0, f1, .5[d0,d1]);
	    fi;
	fi;
    endfor
    drawoptions();

enddef;

beginfig(1);

for rad = .5 step 3 until 300 :
    ellipseFrom( (0,0), (20,0), (30,30), (-1,20), rad );
endfor;

if false:
for r = 1 step 5 until 500:
    drawEllipse( (0,0), (r,.5*r), r * 1.2);
endfor;
fi

clip currentpicture to unitsquare scaled 1000 shifted (-250,-250);

endfig;

end
