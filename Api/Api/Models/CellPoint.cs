namespace Api.Models
{
    public class CellPoint : Coordinate
    {
        public CellPoint()
        {
        }
        
        public CellPoint(CellInfoMongoModel x)
        {
            OperatorName = x.OperatorName;
            CellType = x.CellType;
            Level = x.Level;
            Latitude = x.Location.Latitude;
            Longitude = x.Location.Longitude;
        }
        
        public string OperatorName { get; set; }
        public string CellType { get; set; }
        public int Level { get; set; }
    }
}
