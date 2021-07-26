/***********************************************************/
/* L3 Link Prediction                                      */
/*                                                         */
/* Istvan Kovacs                                           */
/***********************************************************/
#include <math.h>
#include <algorithm>
#include <stdlib.h>
#include <stdio.h>
#include <fstream>
#include <set>
#include <iostream>
#include <time.h>
#include "binheap_2.h"
#include <map>
#include <iomanip>

using namespace std;
typedef std::map<int, double> doublemap;
typedef std::map<int, doublemap> doublematrix;
doublematrix d;
doublemap deg;
long long maxid;
typedef BinaryHeap<double, long long, std::greater_equal<double> >    min_heap;
min_heap                   m_h;
min_heap::pair_type        m_he;

int main(int argc, char** argv)
	{//ido=(int) time(NULL);
	double s;
	int e1,e2;
    int ind;
    char *fg;fg=argv[1];FILE *lg;
    if(!(lg=fopen(fg,"r"))) {fprintf(stderr,"Bemeneti h fajl megnyitasa sikertelen!\n");exit(-1);}
    d.clear();
    
    while (!feof(lg))
        {fscanf(lg,"%d",&e1);
        fscanf(lg,"%d",&e2);
        fscanf(lg,"%lf",&s);
        if (s!=0)
            {ind=0;
            if (d.find(e1)!=d.end())
                {if (d[e1].find(e2)!=d[e1].end())
                    {ind=1;
                    }
                }
            if (ind==0)
                {if (e1!=e2)
                    {d[e1][e2]=s;
                    d[e2][e1]=s;
                    }
                else
                    {d[e1][e1]=s;
                    }
                }
            }
        fscanf(lg,"\n");
        }
    fclose(lg);

    maxid=0;
    for(doublematrix::iterator ii=d.begin(); ii!=d.end(); ii++)
        {deg[ii->first]=0;
        if (maxid<ii->first)
            {maxid=ii->first;
            }
        }
    maxid++;
        
    for(doublematrix::iterator ii=d.begin(); ii!=d.end(); ii++)
        {for(doublemap::iterator ij=ii->second.begin(); ij!=ii->second.end();ij++)
            {if (ii->first<ij->first)
                {deg[ii->first]+=fabs(ij->second);
                deg[ij->first]+=fabs(ij->second);
                }
            else
                {if (ii->first==ij->first)
                    {deg[ii->first]+=fabs(ij->second);
                    }
                }
            }
        }
     
    char lag[255];
    sprintf(lag,"L3_predictions_%s.dat",fg);
    ofstream l(lag, ios::out);
    if (!l)
        {cout << "Error during file operation!\n";
        exit(-1);
        };
    
    doublematrix h2;
    for(doublematrix::iterator i=d.begin(); i!=d.end();i++ )
        {for(doublemap::iterator hi=i->second.begin(); hi!=i->second.end(); hi++)
            {for(doublemap::iterator hj=d[hi->first].begin(); hj!=d[hi->first].end(); hj++)
                {if (hj->first<=i->first)
                    {if (h2.find(i->first)!=h2.end())
                        {if (h2[i->first].find(hj->first)!=h2[i->first].end())
                            {h2[i->first][hj->first]+=1.0/sqrt((double) deg[hi->first]);
                            if(hj->first!=i->first)
                                {h2[hj->first][i->first]+=1.0/sqrt((double) deg[hi->first]);
                                }
                            }
                        else
                            {h2[i->first][hj->first]=1.0/sqrt((double) deg[hi->first]);
                            h2[hj->first][i->first]=1.0/sqrt((double) deg[hi->first]);
                            }
                        }
                    else
                        {h2[i->first][hj->first]=1.0/sqrt((double) deg[hi->first]);
                        h2[hj->first][i->first]=1.0/sqrt((double) deg[hi->first]);
                        }
                    }
                }
            }
        }
        
    doublematrix h3;
    for(doublematrix::iterator i=d.begin(); i!=d.end();i++ )
        {for(doublemap::iterator j=i->second.begin(); j!=i->second.end(); j++)
            {for(doublemap::iterator hi=h2[i->first].begin(); hi!=h2[i->first].end();hi++ )
                {if (hi->first<=j->first)
                    {if (h3.find(hi->first)!=h3.end())
                        {if (h3[hi->first].find(j->first)!=h3[hi->first].end())
                            {h3[hi->first][j->first]+=hi->second/sqrt((double) deg[i->first]);
                            if(hi->first!=j->first)
                                {h3[j->first][hi->first]+=hi->second/sqrt((double) deg[i->first]);
                                }
                            }
                        else
                            {h3[hi->first][j->first]=hi->second/sqrt((double) deg[i->first]);
                            h3[j->first][hi->first]=hi->second/sqrt((double) deg[i->first]);
                            }
                        }
                    else
                        {h3[hi->first][j->first]=hi->second/sqrt((double) deg[i->first]);
                        h3[j->first][hi->first]=hi->second/sqrt((double) deg[i->first]);
                        }
                    }
                }
            }
        }
        
    m_h.clear();
    for(doublematrix::iterator ii=h3.begin(); ii!=h3.end();ii++ )
        {for(doublemap::iterator hi=ii->second.begin(); hi!=ii->second.end(); hi++)
            {if (hi->first<ii->first)
                {if (d[ii->first].find(hi->first)==d[ii->first].end())
                    {m_h.push(hi->second, maxid*((long long) (ii->first))+((long long) (hi->first)));
                    }
                }
            }
        }
        
    int xi, xj;
    while ((!m_h.isEmpty()))
        {m_he = m_h.pop();
        xi=m_he.second/maxid;
        xj=m_he.second-((long long) xi)*maxid;
        if (xi!=xj)
            {l << xi << "\11"<<xj << "\11" << m_he.first<< "\n";
            }
        }
	return 0;
	}
