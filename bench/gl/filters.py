import vob

def benchScene(vs, 
	quad=0,
	nquads=100,
	quadsize=512):
    scene = vob.demo.aniso.bigquad.Scene()
    scene.quad = quad
    scene.nquads = nquads
    scene.quadsize = quadsize

    scene.scene(vs)

    return ("%s %s %s" % (quad, nquads, quadsize * quadsize))

args = {
    # "nquads": (50, 100, 200),
    "nquads": (20),
    "quadsize": (256,512),
    # "quadsize": (512),
    #"quad": (0,1,2,3,4,5,6,7),
    # "quad": (0,5,8,9,10),
    "quad": (5,10),
    # "quad": (5,7),
    }

