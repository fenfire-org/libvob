double get_entropy(const double *pr, int N);

double get_posterior(const double *prior, 
		     const float *likelihood, int event, 
		     double *posterior,
		     int N);

void get_posterior2(double *prior, 
		    float *psi, 
		    double *posterior, 
		    double *data, 
		    int N);
