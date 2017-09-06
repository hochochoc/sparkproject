package NaiveBayesAlgorithm;

public class Matrix {
	private final int m;
	private final int n;
	private final double[][] data;
	
	public Matrix(int m, int n){
		this.m = m;
		this.n = n;
		data = new double[m][n];
	}
	
	public Matrix(double[][] data){
		m = data.length;
		n = data[0].length;
		this.data = new double[m][n];
		for (int i=0; i<m; i++){
			for (int j=0; j<n; j++){
				this.data[i][j]= data[i][j];
			}
		}
	}
	
	public Matrix(Matrix a){
		this(a.data);
	}

	public int getM() {
		return m;
	}

	public int getN() {
		return n;
	}

	public double[][] getData() {
		return data;
	}
	
}
