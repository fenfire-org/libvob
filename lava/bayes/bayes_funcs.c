#include <math.h>

double get_entropy(double *pr, int N) {
    int k;
    double H = 0;
#if 0
    for (k = 0; k < N; k++)
	H -= pr[k] * log(pr[k]);
    return H * M_LOG2E;
#else
    for (k = 0; k < N; k++) {
	double x = pr[k];
	double result;
	asm ("fyl2x" : "=t" (result) : "0" (x), "u" (x) : "st(1)");
	H -= result;
    }
    return H;
#endif
}

double get_posterior(double *prior, 
		     float *likelihood, int event, 
		     double *posterior,
		     int N) {
    int k;
    double p = 0, m;

    if (event)
	for (k = 0; k < N; k++)
	    p += posterior[k] = likelihood[k] * prior[k];
    else
	for (k = 0; k < N; k++)
	    p += posterior[k] = (1 - likelihood[k]) * prior[k];
    
    m = 1 / p;
    for (k = 0; k < N; k++)
	posterior[k] *= m;
    
    return p;
}

void get_posterior2(double *prior, 
		    float *psi, 
		    double *posterior, 
		    double *data, int N) {
    int k;
    double p0;
    double p1 = 0;
    for (k = 0; k < N; k++) p1 += posterior[k + N] = psi[k] * prior[k];
    p0 = 1 - p1;
    
    { 
	double m0 = 1 / p0;
	double m1 = 1 / p1;
	for (k = 0; k < N; k++) {
	    posterior[k] = (prior[k] - posterior[k + N]) * m0;
	    posterior[k + N] *= m1;
	}
    }

    data[0] = p0;
    data[1] = p1;
    data[2] = get_entropy(posterior, N);
    data[3] = get_entropy(posterior + N, N);
}
