// (c) Tuomas J. Lukka

#ifndef VOB_CHILDVS_HXX
#define VOB_CHILDVS_HXX

namespace Vob {

    /** A Vobscene that can be embedded in another.
     * This class doesn't stand by itself - the ObjectStorers
     * are needed to interpret the codes in mapCodes.
     */
    class ChildVS {
    public:
	std::vector<int> mapCodes;
	std::vector<int> coorderInds;
	std::vector<float> coorderFloats;
    };
}

#endif
