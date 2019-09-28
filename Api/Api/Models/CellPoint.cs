namespace Api.Models
{
    public class CellPoint
    {
        public string OperatorName { get; set; }
        public decimal Latitude { get; set; }
        public decimal Longitude { get; set; }
        public string CellType { get; set; }
        public int Level { get; set; }
    }
}
