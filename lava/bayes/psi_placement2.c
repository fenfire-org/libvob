//(C): Janne V. Kujala

// Psi placement computation all in C (see psi_placement.py for an 
// interface)

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

#include "bayes_funcs.h"

static double getTime() {
  struct timeval t;
  gettimeofday(&t, 0);
  return t.tv_usec*1E-6 + t.tv_sec;
}


int placement(float *psi, double *prior, int M, int N, 
	      double *H, double **posterior2) {
    int i;
    double *posterior = malloc(2 * N * sizeof(double));
    double *min_posterior = malloc(2 * N * sizeof(double));
    double min_H = 1E300;
    int min_i;

    double t0 = getTime();

    for (i = 0; i < M; i++, psi += N) {
#if 1
	double data[4];
	double p0, p1, H0, H1;
	get_posterior2(prior, psi, posterior, data, N);
	p0 = data[0];
	p1 = data[1];
	H0 = data[2];
	H1 = data[3];
#elif 0
	int k;
	double p0, p1 = 0, H0 = 0, H1 = 0;
	for (k = 0; k < N; k++) 
	    p1 += posterior[k + N] = psi[k] * prior[k];
	p0 = 1 - p1;

	{ 
	    double m0 = 1 / p0;
	    double m1 = 1 / p1;
	    for (k = 0; k < N; k++) {
		posterior[k] = (prior[k] - posterior[k + N]) * m0;
		posterior[k + N] *= m1;
	    }
	}
	H0 = get_entropy(posterior, N);
	H1 = get_entropy(posterior + N, N);

#else
	double p0 = get_posterior(prior, psi,0, posterior, N);
	double p1 = get_posterior(prior, psi,1, posterior + N, N);
	double H0 = get_entropy(posterior, N);
	double H1 = get_entropy(posterior + N, N);
#endif


	H[i] = p0 * H0 + p1 * H1;
	H[i + M] = H0;
	H[i + 2 * M] = H1;
	    
	if (H[i] < min_H) {
	    double *t = min_posterior;
	    min_posterior = posterior;
	    posterior = t;

	    min_H = H[i];
	    min_i = i;
	}
    }

    {
	double t = getTime() - t0;
	fprintf(stderr, "t = %.3f s\n", t); 
	//fprintf(stderr, "t1 = %.3f\n", t1);
	//fprintf(stderr, "t2 = %.3f\n", t2);
	//fprintf(stderr, "t3 = %.3f\n", t3);
	//fprintf(stderr, "t4 = %.3f\n", t4);
    }

    free(posterior);
    *posterior2 = min_posterior;

    return min_i;
}

float *init_psi(int M, int N) {
    int n;
    char file[] = ",,psi.dat";
    FILE *f;
    float *psi_tbl = malloc(sizeof(float) * M * N);
    
    f = fopen(file, "r");
    if (!f) {
	perror(file);
	abort();
    }

    fprintf(stderr, "Reading psi table...\n");
    n = fread(psi_tbl, sizeof(float), M * N, f);
    fprintf(stderr, "Read %d elements\n", n);
    fclose(f);

    if (n != M * N)
	abort();

    return psi_tbl;
}

int main(int argc, char *argv[]) {
    int i, n;
    float *psi_tbl;
    double *prior;
    double *posterior;
    double *H;

    int M = atoi(argv[1]);
    int N = atoi(argv[2]);

    fprintf(stderr, "%d %d\n", M, N);

    psi_tbl = init_psi(M, N);

    prior = malloc(N * sizeof(double));
    H = malloc(3 * M * sizeof(double));

    fprintf(stderr, "Starting loop...\n");
    while (1) {
	n = fread(prior, sizeof(double), N, stdin);
	//fprintf(stderr, "Read %d elements\n", n); 
	if (n != N) abort();

	//fprintf(stderr, "Computing placement\n");
	i = placement(psi_tbl, prior, M, N, H, &posterior);

	fprintf(stderr, "%d %.15G\n", i, H[i]);
	//fprintf(stderr, "%d %.15G\n", i-1, H[i-1]);
	//fprintf(stderr, "%d %.15G\n", i+1, H[i+1]);

	{
	    double d = i;
	    //fprintf(stderr, "Writing index...\n");
	    n = fwrite(&d, sizeof(d), 1, stdout);
	    //fprintf(stderr, "Wrote %d elements...\n", n);
	}

	//fprintf(stderr, "Writing posteriors...\n");
	n = fwrite(posterior, sizeof(double), 2 * N, stdout);
	//fprintf(stderr, "Wrote %d elements...\n", n);

	//fprintf(stderr, "Writing entropy tables...\n");
	n = fwrite(H, sizeof(double), 3 * M, stdout);
	//fprintf(stderr, "Wrote %d elements...\n", n);
	
	fflush(stdout);

	//memcpy(prior, posterior + r * M, M * sizeof(double));
	free(posterior);
    }

    free(H);
    free(prior);

    return 0;
}

